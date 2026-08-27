package com.letmeknow.studentoffers.notifications

import com.letmeknow.studentoffers.core.time.Clock
import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.flow.first

/**
 * La reverificación: entre programar un recordatorio y dispararlo pueden pasar días, y en
 * esos días el estado pudo cambiar de cuatro maneras distintas.
 *
 * El worker NO notifica con lo que sabía al programar. Vuelve a preguntarle al repositorio y
 * solo entonces decide. Si algo cambió, no notifica y termina en éxito: el aviso quedó
 * obsoleto, no falló.
 *
 * Es una clase y no un `object` porque el repositorio y el reloj son dependencias reales: así
 * las cuatro reglas se testean con fakes, sin WorkManager y sin esperar tiempo real.
 */
class ClaimReminderGate(
    private val repository: PromoRepository,
    private val clock: Clock,
) {

    /**
     * La oferta por la que corresponde notificar, o `null` si el recordatorio ya no aplica.
     *
     * Las cuatro razones para no notificar, en orden:
     * 1. La oferta desapareció del catálogo (el backend la bajó).
     * 2. El usuario dejó de seguirla.
     * 3. El usuario ya la reclamó.
     * 4. Ya venció — o es permanente, en cuyo caso nunca debió programarse.
     */
    suspend fun promoToNotify(promoId: Long): Promo? {
        val promo = repository.promos.first().firstOrNull { it.id == promoId } ?: return null
        if (promoId !in repository.followedIds.first()) return null
        if (promoId in repository.claimedIds.first()) return null

        val expiresAt = promo.expiresAt ?: return null
        return if (expiresAt.isAfter(clock.now())) promo else null
    }
}
