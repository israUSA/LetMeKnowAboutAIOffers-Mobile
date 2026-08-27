package com.letmeknow.aioffers.notifications

import com.letmeknow.aioffers.domain.model.Promo
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Los dos momentos en que se avisa que una oferta seguida sigue sin reclamarse.
 *
 * El nombre del work es estable y derivado del id: reprogramar la misma oferta reemplaza el
 * aviso anterior en vez de sumar uno nuevo (ver [DefaultNotifier]).
 */
enum class ClaimReminderOffset(val daysBefore: Long) {
    THREE_DAYS(3),
    ONE_DAY(1);

    /** `claim-<id>-3d` / `claim-<id>-1d`. */
    fun workName(promoId: Long): String = "claim-$promoId-${daysBefore}d"

    fun triggerAt(expiresAt: Instant): Instant = expiresAt.minus(daysBefore, ChronoUnit.DAYS)
}

/** Un aviso a programar: qué momento es y cuánto falta para él. */
data class ClaimReminder(val offset: ClaimReminderOffset, val delay: Duration)

/**
 * Kotlin puro: decide qué avisos corresponden para una oferta en un instante dado.
 *
 * Vive fuera de [DefaultNotifier] porque la regla es de negocio y se testea sin WorkManager
 * ni Android, igual que `ExpirationRules` o `PromoFilter`.
 *
 * Dos reglas, y ninguna más:
 * - Las ofertas permanentes (`expiresAt == null`) no generan ningún aviso.
 * - Un momento que ya pasó no se programa. Si el usuario sigue una oferta que vence en dos
 *   días, el aviso de 3 días no se inventa: solo queda el de 1 día. Si la sigue a 12 horas
 *   del vencimiento, no queda ninguno.
 *
 * Que la lista venga incompleta no es un caso raro que haya que corregir: es la señal de que
 * [DefaultNotifier] tiene que **cancelar** el work de los momentos ausentes.
 */
object ClaimReminderPlanner {

    fun planFor(promo: Promo, now: Instant): List<ClaimReminder> {
        val expiresAt = promo.expiresAt ?: return emptyList()

        return ClaimReminderOffset.entries.mapNotNull { offset ->
            val delay = Duration.between(now, offset.triggerAt(expiresAt))
            if (delay.isNegative || delay.isZero) null else ClaimReminder(offset, delay)
        }
    }
}
