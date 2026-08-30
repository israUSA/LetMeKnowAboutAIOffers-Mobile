package com.letmeknow.studentoffers.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NotificationsPreferences {

    val enabled: Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)
}

private val Context.notificationPreferences: DataStore<Preferences> by preferencesDataStore(
    name = "notification_prefs",
)

class DataStoreNotificationsPreferences(
    private val dataStore: DataStore<Preferences>,
) : NotificationsPreferences {

    constructor(context: Context) : this(context.applicationContext.notificationPreferences)

    override val enabled: Flow<Boolean> = dataStore.data.map { it[KEY_ENABLED] ?: false }

    override suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_ENABLED] = enabled }
    }

    private companion object {
        val KEY_ENABLED = booleanPreferencesKey("notifications_enabled")
    }
}
