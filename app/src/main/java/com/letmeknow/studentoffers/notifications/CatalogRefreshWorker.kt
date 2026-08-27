package com.letmeknow.studentoffers.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.letmeknow.studentoffers.App
import com.letmeknow.studentoffers.domain.model.Promo
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Refresca el catálogo cada ~6 horas y avisa si aparecieron ofertas nuevas.
 *
 * Es la aproximación sin backend a "hay una oferta nueva": no hay push, así que lo único
 * disponible es traer el catálogo cada tanto y comparar ids contra lo que ya había en Room.
 *
 * **La latencia es parte del contrato, no un bug.** Doze, App Standby y la ventana flexible
 * de WorkManager hacen que "cada 6 horas" signifique "cada 6 horas o más, cuando al sistema
 * le venga bien". Pelearse con eso (alarmas exactas, foreground services) sería gastar batería
 * del usuario para adelantar un aviso informativo. El período mínimo de WorkManager es de 15
 * minutos y este está muy por encima, así que no hay nada que ajustar.
 */
class CatalogRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? App)?.container ?: return Result.success()
        // Sin configuración no hay red que consultar y construir el repositorio lanzaría.
        // Éxito, no fallo: reintentar no va a hacer aparecer las claves.
        val repository = container.promoRepositoryOrNull() ?: return Result.success()

        val knownIds = repository.promos.first().mapTo(mutableSetOf(), Promo::id)

        // La red falló: se reintenta con el backoff de WorkManager. Es el único caso que
        // no termina en éxito, y el único en el que reintentar tiene sentido.
        repository.refresh().getOrElse { return Result.retry() }

        val newPromos = NewPromoDetector.newPromos(knownIds, repository.promos.first())
        container.notifier.notifyNewPromos(newPromos)

        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "catalog-refresh"
        private const val INTERVAL_HOURS = 6L

        /**
         * Encola el refresco periódico, una sola vez en la vida de la instalación.
         *
         * [ExistingPeriodicWorkPolicy.KEEP] es lo que hace que llamarlo en cada arranque sea
         * inocuo: si el work ya existe se respeta su período en curso en vez de reiniciarle
         * el reloj, que es lo que haría que en una app que se abre seguido no corriera nunca.
         */
        fun ensureScheduled(context: Context) {
            val request = PeriodicWorkRequestBuilder<CatalogRefreshWorker>(
                INTERVAL_HOURS,
                TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
