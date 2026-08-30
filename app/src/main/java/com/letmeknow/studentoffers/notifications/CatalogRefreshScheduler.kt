package com.letmeknow.studentoffers.notifications

import android.content.Context
import com.letmeknow.studentoffers.data.local.NotificationsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class CatalogRefreshScheduler(
    private val context: Context,
    private val preferences: NotificationsPreferences,
) {

    fun start(scope: CoroutineScope) {
        preferences.enabled
            .distinctUntilChanged()
            .onEach { enabled -> apply(enabled) }
            .launchIn(scope)
    }

    private fun apply(enabled: Boolean) {
        try {
            if (enabled) {
                CatalogRefreshWorker.ensureScheduled(context)
            } else {
                CatalogRefreshWorker.cancel(context)
            }
        } catch (_: IllegalStateException) {
        }
    }
}
