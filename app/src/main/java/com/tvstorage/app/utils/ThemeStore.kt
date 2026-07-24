package com.tvstorage.app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeStore(private val context: Context) {
    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
        val LAST_VERSION_KEY = stringPreferencesKey("last_version")
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
