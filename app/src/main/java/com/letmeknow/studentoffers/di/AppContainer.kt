package com.letmeknow.studentoffers.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.letmeknow.studentoffers.core.config.AppConfig
import com.letmeknow.studentoffers.core.time.Clock
import com.letmeknow.studentoffers.data.DefaultPromoRepository
import com.letmeknow.studentoffers.data.PromoRepository
import com.letmeknow.studentoffers.data.local.AppDatabase
import com.letmeknow.studentoffers.data.local.DataStoreNotificationsPreferences
import com.letmeknow.studentoffers.data.local.DataStorePrefsDataSource
import com.letmeknow.studentoffers.data.local.NotificationsPreferences
import com.letmeknow.studentoffers.data.local.PrefsDataSource
import com.letmeknow.studentoffers.data.local.PromoLocalDataSource
import com.letmeknow.studentoffers.data.local.RoomPromoLocalDataSource
import com.letmeknow.studentoffers.data.remote.PromosApi
import com.letmeknow.studentoffers.data.remote.PromosRemoteDataSource
import com.letmeknow.studentoffers.data.remote.SupabaseAuthInterceptor
import com.letmeknow.studentoffers.domain.ExpirationRules
import com.letmeknow.studentoffers.feature.promos.PromosViewModel
import com.letmeknow.studentoffers.notifications.CatalogRefreshScheduler
import com.letmeknow.studentoffers.notifications.DefaultNotifier
import com.letmeknow.studentoffers.notifications.Notifier
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(
    private val context: Context,
    val clock: Clock = Clock.System,
) {
    val config: AppConfig by lazy { AppConfig.read() }

    val expirationRules: ExpirationRules by lazy { ExpirationRules(clock) }

    private val json: Json by lazy {
        Json { ignoreUnknownKeys = true }
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    private val localDataSource: PromoLocalDataSource by lazy {
        RoomPromoLocalDataSource(database.promoDao())
    }

    private val prefsDataSource: PrefsDataSource by lazy { DataStorePrefsDataSource(context) }

    val notificationsPreferences: NotificationsPreferences by lazy {
        DataStoreNotificationsPreferences(context)
    }

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

    val promoRepository: PromoRepository by lazy {
        DefaultPromoRepository(
            remote = PromosRemoteDataSource(promosApi, json),
            local = localDataSource,
            prefs = prefsDataSource,
        )
    }

    fun promoRepositoryOrNull(): PromoRepository? =
        if (config is AppConfig.Valid) promoRepository else null

    val notifier: Notifier by lazy { DefaultNotifier(context.applicationContext) }

    val catalogRefreshScheduler: CatalogRefreshScheduler by lazy {
        CatalogRefreshScheduler(
            context = context.applicationContext,
            preferences = notificationsPreferences,
        )
    }

    val promosViewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                PromosViewModel(
                    config = config,
                    rules = expirationRules,
                    repositoryProvider = { promoRepository },
                    notificationsPreferences = notificationsPreferences,
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
