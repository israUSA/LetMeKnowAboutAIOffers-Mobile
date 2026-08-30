package com.letmeknow.studentoffers.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PrefsDataSource {

    val claimedIds: Flow<Set<Long>>

    suspend fun markClaimed(promoId: Long)
}

private val Context.promoPreferences: DataStore<Preferences> by preferencesDataStore(name = "promo_prefs")

class DataStorePrefsDataSource(private val dataStore: DataStore<Preferences>) : PrefsDataSource {

    constructor(context: Context) : this(context.applicationContext.promoPreferences)

    override val claimedIds: Flow<Set<Long>> = dataStore.data.map { it.readIds(KEY_CLAIMED) }

    override suspend fun markClaimed(promoId: Long) {
        dataStore.edit { prefs ->
            prefs[KEY_CLAIMED] = prefs[KEY_CLAIMED].orEmpty() + promoId.toString()
        }
    }

    private fun Preferences.readIds(key: Preferences.Key<Set<String>>): Set<Long> =
        this[key].orEmpty().mapNotNullTo(mutableSetOf(), String::toLongOrNull)

    private companion object {
        val KEY_CLAIMED = stringSetPreferencesKey("claimed_ids")
    }
}
