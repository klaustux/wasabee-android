package rocks.wasabee.android.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("session")

data class Session(val server: WasabeeServer, val jwt: String)

class SessionStore(private val context: Context) {

    suspend fun load(): Session? {
        val prefs = context.dataStore.data.first()
        val server = prefs[SERVER]?.let { runCatching { WasabeeServer.valueOf(it) }.getOrNull() } ?: return null
        val jwt = prefs[JWT] ?: return null
        return Session(server, jwt)
    }

    suspend fun save(session: Session) {
        context.dataStore.edit {
            it[SERVER] = session.server.name
            it[JWT] = session.jwt
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    private companion object {
        val SERVER = stringPreferencesKey("server")
        val JWT = stringPreferencesKey("jwt")
    }
}
