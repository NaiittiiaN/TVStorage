package com.tvstorage.app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeStore(private val context: Context) {
    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val LAST_VERSION_KEY = stringPreferencesKey("last_version")
        val WEB_PORT_KEY = intPreferencesKey("web_port")
    }

    val webPort: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[WEB_PORT_KEY] ?: 4848
    }

    suspend fun setWebPort(port: Int) {
        context.dataStore.edit { preferences ->
            preferences[WEB_PORT_KEY] = port
        }
    }

    val lastSeenVersion: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_VERSION_KEY]
    }

    suspend fun setLastSeenVersion(version: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_VERSION_KEY] = version
        }
    }

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY]
    }

    suspend fun setDarkTheme(isDark: Boolean?) {
        context.dataStore.edit { preferences ->
            if (isDark == null) {
                preferences.remove(DARK_THEME_KEY)
            } else {
                preferences[DARK_THEME_KEY] = isDark
            }
        }
    }
}
