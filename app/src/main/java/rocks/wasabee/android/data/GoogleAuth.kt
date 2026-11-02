package rocks.wasabee.android.data

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

sealed interface GoogleAuthResult {
    data class Token(val accessToken: String) : GoogleAuthResult
    data class NeedsConsent(val pendingIntent: PendingIntent) : GoogleAuthResult
}

/**
 * Google access token for /aptok. The server only calls Google's userinfo with it,
 * so any OAuth client registered for this package + signing SHA-1 works.
 */
object GoogleAuth {

    private val request = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope("email"), Scope("profile")))
        .build()

    suspend fun authorize(activity: Activity): GoogleAuthResult {
        val result = Identity.getAuthorizationClient(activity).authorize(request).await()
        val pendingIntent = result.pendingIntent
        return if (result.hasResolution() && pendingIntent != null) {
            GoogleAuthResult.NeedsConsent(pendingIntent)
        } else {
            GoogleAuthResult.Token(checkNotNull(result.accessToken) { "Google returned no access token" })
        }
    }

    fun tokenFromConsent(activity: Activity, data: Intent?): String {
        val result = Identity.getAuthorizationClient(activity).getAuthorizationResultFromIntent(data)
        return checkNotNull(result.accessToken) { "Google returned no access token" }
    }
}
