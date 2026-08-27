package com.letmeknow.studentoffers.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Preferencias del usuario: qué ofertas sigue y cuáles ya reclamó.
 *
 * Interfaz aparte de la implementación por el mismo motivo que [PromoLocalDataSource]: el
 * repositorio se testea en la JVM, donde DataStore no arranca.
 */
interface PrefsDataSource {

    /** Ids que el usuario marcó para recibir aviso antes de que venzan. */
    val followedIds: Flow<Set<Long>>

    /** Ids cuyo CTA "Reclamar" ya se tocó. Se usa para no recordarle de más. */
    val claimedIds: Flow<Set<Long>>

    suspend fun setFollowed(promoId: Long, followed: Boolean)

    suspend fun markClaimed(promoId: Long)
}

/** El delegate crea el archivo perezosamente, en el primer acceso, no al declararse. */
private val Context.promoPreferences: DataStore<Preferences> by preferencesDataStore(name = "promo_prefs")

/**
 * Los ids se guardan como `Set<String>` porque DataStore Preferences no tiene un tipo de
 * conjunto de números. La conversión vive acá y no se filtra al resto de la app: la interfaz
 * habla de `Set<Long>`, igual que el dominio.
 *
 * Una entrada que no parsea a `Long` se descarta en vez de reventar: son preferencias de
 * usuario, no datos de negocio, y una preferencia corrupta no puede tumbar la pantalla.
 */
class DataStorePrefsDataSource(private val dataStore: DataStore<Preferences>) : PrefsDataSource {

    constructor(context: Context) : this(context.applicationContext.promoPreferences)

    override val followedIds: Flow<Set<Long>> = dataStore.data.map { it.readIds(KEY_FOLLOWED) }

    override val claimedIds: Flow<Set<Long>> = dataStore.data.map { it.readIds(KEY_CLAIMED) }

    override suspend fun setFollowed(promoId: Long, followed: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_FOLLOWED].orEmpty()
            prefs[KEY_FOLLOWED] = if (followed) current + promoId.toString() else current - promoId.toString()
        }
    }

    override suspend fun markClaimed(promoId: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_CLAIMED] = prefs[KEY_CLAIMED].orEmpty() + promoId.toString()
        }
    }

    private fun Preferences.readIds(key: Preferences.Key<Set<String>>): Set<Long> =
        this[key].orEmpty().mapNotNullTo(mutableSetOf(), String::toLongOrNull)

    private companion object {
        val KEY_FOLLOWED = stringSetPreferencesKey("followed_ids")
        val KEY_CLAIMED = stringSetPreferencesKey("claimed_ids")
    }
}
