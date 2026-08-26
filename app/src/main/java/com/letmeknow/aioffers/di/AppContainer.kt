package com.letmeknow.aioffers.di

import android.content.Context
import com.letmeknow.aioffers.core.config.AppConfig
import com.letmeknow.aioffers.core.time.Clock

/**
 * DI manual. La app tiene un endpoint y ninguna sesión de usuario; Hilt sería ceremonia sin
 * beneficio, y su `@Provides` eager es justamente el patrón que reintroduce el bug de la web.
 *
 * REGLA INNEGOCIABLE: todo lo que dependa de la red se construye con `by lazy` y solo detrás
 * de un [AppConfig.Valid]. Nada de este contenedor puede lanzar durante `Application.onCreate`.
 * Ver el comentario extenso en [AppConfig].
 */
class AppContainer(
    @Suppress("unused") private val context: Context,
    val clock: Clock = Clock.System,
) {
    /** Se evalúa una vez, no lanza nunca, y decide si hay app o pantalla de error. */
    val config: AppConfig by lazy { AppConfig.read() }

    // TODO(feat/data): repository, api, database y prefs, todos `by lazy` y solo si
    //  `config is AppConfig.Valid`. Si es `Missing`, el ViewModel emite
    //  PromosUiState.Error(ErrorKind.MissingConfig) sin tocar nada de red.

    // TODO(feat/notifications): notifier y scheduler de WorkManager.
}
