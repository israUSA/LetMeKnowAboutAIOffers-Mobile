package com.letmeknow.aioffers.notifications

import com.letmeknow.aioffers.domain.model.Promo

/**
 * Frontera de las notificaciones.
 *
 * Hoy hay una sola implementación, local, basada en WorkManager: los recordatorios se
 * programan en el dispositivo porque `expires_at` ya viene en el payload y no hace falta
 * backend. La interfaz existe para que enchufar FCM en fase 2 sea agregar una
 * implementación, no reescribir el feature.
 */
interface Notifier {

    /** Recordatorio de "todavía no reclamaste esta oferta y está por vencer". */
    suspend fun scheduleClaimReminder(promo: Promo)

    /** El usuario dejó de seguir la oferta, ya la reclamó, o la oferta desapareció. */
    suspend fun cancelClaimReminder(promoId: Long)

    /** Reconcilia todos los recordatorios programados contra el estado actual. */
    suspend fun rescheduleAll()

    /** Aviso de ofertas nuevas detectadas al refrescar el catálogo. */
    suspend fun notifyNewPromos(promos: List<Promo>)
}
