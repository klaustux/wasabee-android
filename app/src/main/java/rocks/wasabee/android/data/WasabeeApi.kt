package rocks.wasabee.android.data

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WasabeeApi {
    @POST("/aptok")
    suspend fun loginWithGoogle(@Body body: AccessTokenBody): Me

    @Multipart
    @POST("/oneTimeToken")
    suspend fun loginWithOneTimeToken(@Part("token") token: RequestBody): Me

    @GET("me")
    suspend fun me(): Me
}
