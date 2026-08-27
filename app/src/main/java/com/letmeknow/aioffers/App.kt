package com.letmeknow.aioffers

import android.app.Application
import com.letmeknow.aioffers.di.AppContainer
import com.letmeknow.aioffers.notifications.CatalogRefreshWorker
import com.letmeknow.aioffers.notifications.NotificationChannels

class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Construir el contenedor no puede fallar: todo lo que puede faltar (URL, key) se
        // resuelve perezosamente y se reporta como estado de error en la UI, nunca como crash
        // al arrancar. Ver DATA_AND_API.md, "Lección aprendida en la web".
        container = AppContainer(applicationContext)

        // Crear canales no toca red, ni disco de la app, ni configuración: es seguro acá y es
        // idempotente, así que no hace falta llevar la cuenta de si ya se hizo.
        NotificationChannels.ensureCreated(this)

        scheduleCatalogRefresh()
    }

    /**
     * El refresco periódico se encola acá y no en la Activity porque tiene que quedar armado
     * aunque la app se levante por un broadcast y el usuario nunca abra una pantalla.
     *
     * Va envuelto en `try/catch` por la misma regla que gobierna todo este arranque: WorkManager
     * se inicializa solo, desde su `ContentProvider`, antes de este `onCreate` — pero si esa
     * inicialización falla, la app tiene que seguir abriendo. Quedarse sin refresco en
     * background es una degradación; una pantalla en blanco al arrancar es el bug documentado
     * en DATA_AND_API.md.
     */
    private fun scheduleCatalogRefresh() {
        try {
            CatalogRefreshWorker.ensureScheduled(this)
        } catch (_: IllegalStateException) {
            // WorkManager no está inicializado. Sin refresco periódico, con app.
        }
    }
}
