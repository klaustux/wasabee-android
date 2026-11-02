package rocks.wasabee.android.data

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import rocks.wasabee.android.BuildConfig

class WasabeeRepository(private val sessionStore: SessionStore) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private var session: Session? = null

    suspend fun restoreSession(): Me? {
        session = sessionStore.load() ?: return null
        return me()
    }

    suspend fun loginWithGoogle(server: WasabeeServer, accessToken: String): Me =
        api(server, jwt = null).loginWithGoogle(AccessTokenBody(accessToken)).also { storeSession(server, it) }

    suspend fun loginWithOneTimeToken(server: WasabeeServer, token: String): Me =
        api(server, jwt = null)
            .loginWithOneTimeToken(token.trim().toRequestBody("text/plain".toMediaType()))
            .also { storeSession(server, it) }

    suspend fun me(): Me {
        val current = checkNotNull(session) { "Not logged in" }
        return api(current.server, current.jwt).me()
    }

    suspend fun logout() {
        session = null
        sessionStore.clear()
    }

    private suspend fun storeSession(server: WasabeeServer, me: Me) {
        val jwt = checkNotNull(me.jwt) { "Server did not return a token" }
        session = Session(server, jwt).also { sessionStore.save(it) }
    }

    private fun api(server: WasabeeServer, jwt: String?): WasabeeApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "WasabeeAndroid/${BuildConfig.VERSION_NAME}")
                    .apply { jwt?.let { header("Authorization", "Bearer $it") } }
                    .build()
                chain.proceed(request)
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("${server.baseUrl}/api/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WasabeeApi::class.java)
    }
}
