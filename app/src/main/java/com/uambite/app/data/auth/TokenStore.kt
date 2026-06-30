package com.uambite.app.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.uambite.app.data.api.AuthResponse
import com.uambite.app.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "uambite_auth")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val USER_KEY = stringPreferencesKey("user")
    }

    val token: Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    val user: Flow<User?> = dataStore.data.map { preferences ->
        preferences[USER_KEY]?.let { json.decodeFromString<AuthResponse>(it).toDomain() }
    }

    val isLoggedIn: Flow<Boolean> = token.map { it != null }

    suspend fun saveSession(authResponse: AuthResponse) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = authResponse.token
            preferences[USER_KEY] = json.encodeToString(authResponse)
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_KEY)
        }
    }

    suspend fun markPasswordChanged() {
        dataStore.edit { preferences ->
            preferences[USER_KEY]?.let { userJson ->
                val auth = json.decodeFromString<AuthResponse>(userJson)
                preferences[USER_KEY] = json.encodeToString(auth.copy(requiereCambioPassword = false))
            }
        }
    }

    private fun AuthResponse.toDomain(): User = User(
        id = id,
        carnet = carnet,
        nombre = nombre,
        apellido = apellido,
        correo = correo,
        rol = rol,
        requiereCambioPassword = requiereCambioPassword
    )
}
