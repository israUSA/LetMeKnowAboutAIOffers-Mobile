package com.letmeknow.studentoffers.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.letmeknow.studentoffers.core.time.Clock
import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.flow.first

/**
 * Implementación local de [Notifier], sobre WorkManager.
 *
 * No hay backend ni push: `expiresAt` ya viene en el payload, así que los recordatorios se
 * calculan y se programan en el dispositivo. La regla de negocio ("a 3 días y a 1 día del
 * vencimiento, salvo permanentes") vive en [ClaimReminderPlanner], que es Kotlin puro; acá
 * solo queda la traducción a work únicos.
 *
 * **Idempotencia por diseño.** Cada aviso es un work único con nombre estable
 * (`claim-<id>-3d` / `claim-<id>-1d`) encolado con [ExistingWorkPolicy.REPLACE]: reprogramar
 * la misma oferta cien veces deja siempre exactamente un work por momento. Y cuando el plan
 * ya no incluye un momento (porque pasó, o porque la oferta se volvió permanente), ese work se
 * **cancela** en vez de dejarse huérfano — si no, un aviso viejo seguiría vivo esperando
 * dispararse por una regla que ya no existe.
 *
 * @param repositoryProvider devuelve `null` cuando falta configuración. Es perezoso y
 *   anulable a propósito: [rescheduleAll] corre desde el `BootReceiver`, donde construir el
 *   repositorio sin configuración válida lanzaría (ver `AppConfig`).
 */
class DefaultNotifier(
    private val context: Context,
    private val clock: Clock,
    private val repositoryProvider: () -> PromoRepository?,
) : Notifier {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    private val presenter: PromoNotificationPresenter by lazy {
        PromoNotificationPresenter(context)
    }

    override suspend fun scheduleClaimReminder(promo: Promo) {
        val planned = ClaimReminderPlanner.planFor(promo, clock.now()).associateBy { it.offset }

        // Se recorren SIEMPRE los dos momentos, no solo los planificados: el que quedó fuera
        // del plan es justamente el que hay que cancelar.
        ClaimReminderOffset.entries.forEach { offset ->
            val name = offset.workName(promo.id)
            val reminder = planned[offset]

            if (reminder == null) {
                workManager.cancelUniqueWork(name)
            } else {
                workManager.enqueueUniqueWork(
                    name,
                    ExistingWorkPolicy.REPLACE,
                    ClaimReminderWorker.request(promo.id, offset, reminder.delay),
                )
            }
        }
    }

    override suspend fun cancelClaimReminder(promoId: Long) {
        ClaimReminderOffset.entries.forEach { offset ->
            workManager.cancelUniqueWork(offset.workName(promoId))
        }
    }

    /**
     * Reconcilia todo contra el estado actual. Es lo que corre después de un reinicio, cuando
     * WorkManager perdió los delays pendientes.
     *
     * Recorre lo que el usuario sigue, no el catálogo: una oferta que dejó de seguirse ya no
     * tiene nada que reprogramar, y una que sigue pero desapareció del backend o ya se reclamó
     * se cancela acá.
     */
    override suspend fun rescheduleAll() {
        val repository = repositoryProvider() ?: return

        val catalog = repository.promos.first().associateBy(Promo::id)
        val followed = repository.followedIds.first()
        val claimed = repository.claimedIds.first()

        followed.forEach { promoId ->
            val promo = catalog[promoId]
            if (promo == null || promoId in claimed) {
                cancelClaimReminder(promoId)
            } else {
                scheduleClaimReminder(promo)
            }
        }
    }

    override suspend fun notifyNewPromos(promos: List<Promo>) {
        if (promos.isEmpty()) return
        presenter.showNewPromos(promos)
    }
}
