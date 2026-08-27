package com.letmeknow.aioffers.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.letmeknow.aioffers.core.config.AppConfig
import com.letmeknow.aioffers.core.time.Clock
import com.letmeknow.aioffers.data.DefaultPromoRepository
import com.letmeknow.aioffers.data.PromoRepository
import com.letmeknow.aioffers.data.local.AppDatabase
import com.letmeknow.aioffers.data.local.DataStorePrefsDataSource
import com.letmeknow.aioffers.data.local.PrefsDataSource
import com.letmeknow.aioffers.data.local.PromoLocalDataSource
import com.letmeknow.aioffers.data.local.RoomPromoLocalDataSource
import com.letmeknow.aioffers.data.remote.PromosApi
import com.letmeknow.aioffers.data.remote.PromosRemoteDataSource
import com.letmeknow.aioffers.data.remote.SupabaseAuthInterceptor
import com.letmeknow.aioffers.domain.ExpirationRules
import com.letmeknow.aioffers.feature.promos.PromosViewModel
import com.letmeknow.aioffers.notifications.DefaultNotifier
import com.letmeknow.aioffers.notifications.Notifier
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * DI manual. La app tiene un endpoint y ninguna sesión de usuario; Hilt sería ceremonia sin
 * beneficio, y su `@Provides` eager es justamente el patrón que reintroduce el bug de la web.
 *
 * REGLA INNEGOCIABLE: todo lo que dependa de la red se construye con `by lazy` y solo detrás
 * de un [AppConfig.Valid]. Nada de este contenedor puede lanzar durante `Application.onCreate`.
 * Ver el comentario extenso en [AppConfig].
 *
 * Cómo se cumple acá, concretamente:
 * - Ningún `val` de red, base de datos o preferencias se evalúa al construir el contenedor.
 * - [promoRepository] exige un [AppConfig.Valid]; el único que lo toca es [PromosViewModel],
 *   y solo después de haber comprobado la configuración. Con [AppConfig.Missing] la cadena
 *   entera queda sin evaluar y la pantalla muestra `ErrorKind.MissingConfig`.
 */
class AppContainer(
    private val context: Context,
    val clock: Clock = Clock.System,
) {
    /** Se evalúa una vez, no lanza nunca, y decide si hay app o pantalla de error. */
    val config: AppConfig by lazy { AppConfig.read() }

    /** Kotlin puro: no depende de configuración, así que se puede construir siempre. */
    val expirationRules: ExpirationRules by lazy { ExpirationRules(clock) }

    private val json: Json by lazy {
        // El backend puede agregar campos (`start_date` ya es uno que no se usa) sin que eso
        // deba romper el parseo.
        Json { ignoreUnknownKeys = true }
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, AppDatabase.NAME)
            // El caché es reconstruible desde `promos-batch`: perderlo en una migración no
            // pierde nada del usuario, y una migración fallida sí dejaría la app inutilizable.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    private val localDataSource: PromoLocalDataSource by lazy {
        RoomPromoLocalDataSource(database.promoDao())
    }

    private val prefsDataSource: PrefsDataSource by lazy { DataStorePrefsDataSource(context) }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(SupabaseAuthInterceptor(requireValidConfig().anonKey))
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private val promosApi: PromosApi by lazy {
        Retrofit.Builder()
            .baseUrl(requireValidConfig().functionsBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
            .build()
            .create(PromosApi::class.java)
    }

    /**
     * Solo se puede tocar con [AppConfig.Valid]. El `checkNotNull` de [requireValidConfig] no
     * es una red de seguridad opcional: es lo que convierte "alguien construyó el cliente HTTP
     * sin configuración" en un error de programación ruidoso y no en una pantalla en blanco.
     */
    val promoRepository: PromoRepository by lazy {
        DefaultPromoRepository(
            remote = PromosRemoteDataSource(promosApi, json),
            local = localDataSource,
            prefs = prefsDataSource,
        )
    }

    /**
     * Devuelve el repositorio solo si hay configuración válida.
     *
     * Existe para los consumidores que corren **fuera** de la UI —los workers de
     * notificaciones y el `BootReceiver`—, que no pasaron por el chequeo de configuración que
     * hace `PromosViewModel`. Sin esto tendrían que tocar [promoRepository] a ciegas y
     * reventarían con `AppConfig.Missing` en un lugar donde no hay pantalla que mostrar.
     */
    fun promoRepositoryOrNull(): PromoRepository? =
        if (config is AppConfig.Valid) promoRepository else null

    /**
     * Frontera de notificaciones. Perezoso como todo lo demás: construirlo no toca WorkManager
     * ni el repositorio, así que crear el contenedor sigue sin poder fallar.
     */
    val notifier: Notifier by lazy {
        DefaultNotifier(
            context = context.applicationContext,
            clock = clock,
            repositoryProvider = ::promoRepositoryOrNull,
        )
    }

    /**
     * Fábrica del ViewModel de la pantalla principal.
     *
     * El repositorio se pasa como lambda, no como instancia: construirlo acá lo evaluaría
     * aunque la configuración falte, que es exactamente el bug que se está evitando.
     */
    val promosViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                PromosViewModel(
                    config = config,
                    rules = expirationRules,
                    repositoryProvider = { promoRepository },
                    notifierProvider = { notifier },
                )
            }
        }
    }

    private fun requireValidConfig(): AppConfig.Valid = checkNotNull(config as? AppConfig.Valid) {
        "Se intentó usar la red con configuración ausente. Con AppConfig.Missing la pantalla " +
            "debe mostrar ErrorKind.MissingConfig sin construir nada de esta cadena."
    }

    private companion object {
        const val TIMEOUT_SECONDS = 20L
        const val JSON_MEDIA_TYPE = "application/json"
    }
}
