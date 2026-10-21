package malo.mrn.tunetalk.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import malo.mrn.tunetalk.domain.model.UserSession
import java.time.Instant

private val Context.sessionDataStore by preferencesDataStore(name = "tunetalk_session")

class SessionStore(context: Context) {
    private val dataStore = context.applicationContext.sessionDataStore

    suspend fun read(): UserSession? {
        val preferences = dataStore.data.first()
        val token = preferences[AccessToken] ?: return null
        val email = preferences[Email] ?: return null
        val displayName = preferences[DisplayName] ?: return null
        val expiresAt = preferences[ExpiresAt] ?: return null

        if (expiresAt <= Instant.now().epochSecond) {
            clear()
            return null
        }

        return UserSession(token, email, displayName, expiresAt)
    }

    suspend fun write(session: UserSession) {
        dataStore.edit { preferences ->
            preferences[AccessToken] = session.accessToken
            preferences[Email] = session.email
            preferences[DisplayName] = session.displayName
            preferences[ExpiresAt] = session.expiresAtEpochSeconds
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(AccessToken)
            preferences.remove(Email)
            preferences.remove(DisplayName)
            preferences.remove(ExpiresAt)
        }
    }

    private companion object {
        val AccessToken = stringPreferencesKey("access_token")
        val Email = stringPreferencesKey("email")
        val DisplayName = stringPreferencesKey("display_name")
        val ExpiresAt = longPreferencesKey("expires_at")
    }
}
