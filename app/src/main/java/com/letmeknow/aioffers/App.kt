package com.letmeknow.aioffers

import android.app.Application
import com.letmeknow.aioffers.di.AppContainer

class App : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // Construir el contenedor no puede fallar: todo lo que puede faltar (URL, key) se
        // resuelve perezosamente y se reporta como estado de error en la UI, nunca como crash
        // al arrancar. Ver DATA_AND_API.md, "Lección aprendida en la web".
        container = AppContainer(applicationContext)
    }
}
