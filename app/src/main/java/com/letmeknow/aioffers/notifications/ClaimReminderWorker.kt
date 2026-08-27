package com.letmeknow.aioffers.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.letmeknow.aioffers.App
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Dispara un recordatorio de reclamo… si todavía corresponde.
 *
 * **La reverificación es el punto entero de este worker.** Entre que se programó y que se
 * ejecuta pasaron días: la oferta pudo reclamarse, vencerse, dejarse de seguir o desaparecer
 * del catálogo. Notificar con el estado congelado del momento de programar sería avisar de
 * algo que ya no es cierto, así que el worker le vuelve a preguntar al repositorio
 * ([ClaimReminderGate]) y recién ahí decide.
 *
 * Todos los caminos de "ya no corresponde" terminan en [Result.success]: el aviso quedó
 * obsoleto, no falló. Reintentarlo no lo volvería relevante.
 */
class ClaimReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val promoId = inputData.getLong(KEY_PROMO_ID, INVALID_ID)
        val offset = inputData.getString(KEY_OFFSET)?.let { name ->
            ClaimReminderOffset.entries.firstOrNull { it.name == name }
        }
        if (promoId == INVALID_ID || offset == null) return Result.success()

        val container = (applicationContext as? App)?.container ?: return Result.success()
        // Sin configuración no hay repositorio contra el cual reverificar, y construirlo
        // lanzaría. Se termina en éxito: la app ya muestra su pantalla de error.
        val repository = container.promoRepositoryOrNull() ?: return Result.success()

        val promo = ClaimReminderGate(repository, container.clock).promoToNotify(promoId)
            ?: return Result.success()

        PromoNotificationPresenter(applicationContext).showClaimReminder(promo, offset)
        return Result.success()
    }

    companion object {
        private const val KEY_PROMO_ID = "promo_id"
        private const val KEY_OFFSET = "offset"
        private const val INVALID_ID = -1L

        fun request(promoId: Long, offset: ClaimReminderOffset, delay: Duration): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<ClaimReminderWorker>()
                .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .setInputData(inputData(promoId, offset))
                .build()

        fun inputData(promoId: Long, offset: ClaimReminderOffset): Data =
            workDataOf(KEY_PROMO_ID to promoId, KEY_OFFSET to offset.name)
    }
}
