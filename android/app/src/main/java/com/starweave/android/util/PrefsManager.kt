package com.starweave.android.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.starweave.android.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "starweave_prefs")

class PrefsManager(private val context: Context) {
    companion object {
        private val KEY_USER_ID = longPreferencesKey("user_id")
        private val KEY_USER_JSON = stringPreferencesKey("user_json")
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
    }

    private val gson = Gson()

    val userId: Flow<Long?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: 0L
        if (id > 0) id else null
    }

    val userJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_JSON]
    }

    val user: Flow<User?> = userJson.map { json ->
        json?.let { gson.fromJson(it, User::class.java) }
    }

    val baseUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_BASE_URL]
    }

    suspend fun saveUser(user: User) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_USER_JSON] = gson.toJson(user)
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_USER_JSON)
        }
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = url
        }
    }
}
