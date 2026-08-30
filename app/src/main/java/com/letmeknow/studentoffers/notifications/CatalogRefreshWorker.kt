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

class CatalogRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as? App)?.container ?: return Result.success()
        val repository = container.promoRepositoryOrNull() ?: return Result.success()

        val knownIds = repository.promos.first().mapTo(mutableSetOf(), Promo::id)

        repository.refresh().getOrElse { return Result.retry() }

        val newPromos = NewPromoDetector.newPromos(knownIds, repository.promos.first())
        container.notifier.notifyNewPromos(newPromos)

        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "catalog-refresh"
        private const val INTERVAL_HOURS = 6L

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

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
        }
    }
}
