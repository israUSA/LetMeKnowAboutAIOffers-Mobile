package com.letmeknow.studentoffers

import android.app.Application
import com.letmeknow.studentoffers.di.AppContainer
import com.letmeknow.studentoffers.notifications.NotificationChannels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)

        NotificationChannels.ensureCreated(this)

        scheduleCatalogRefresh()
    }

    private fun scheduleCatalogRefresh() {
        container.catalogRefreshScheduler.start(appScope)
    }
}
