package rocks.wasabee.android.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Me(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("pic") val picture: String? = null,
    @SerialName("Teams") val teams: List<MeTeam> = emptyList(),
    @SerialName("Ops") val ops: List<MeOp> = emptyList(),
    @SerialName("jwt") val jwt: String? = null,
)

@Serializable
data class MeTeam(
    @SerialName("ID") val id: String,
    @SerialName("Name") val name: String = "",
    @SerialName("State") val state: String = "",
    @SerialName("Owner") val owner: String = "",
)

@Serializable
data class MeOp(
    @SerialName("ID") val id: String,
    @SerialName("Name") val name: String = "",
    @SerialName("IsOwner") val isOwner: Boolean = false,
    @SerialName("Color") val color: String = "",
    @SerialName("TeamID") val teamId: String = "",
    @SerialName("Modified") val modified: String = "",
)

@Serializable
data class AccessTokenBody(
    @SerialName("accessToken") val accessToken: String,
)
