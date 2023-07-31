package xyz.bluspring.sprinkles.twitch.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchAuthConfig(
    @SerialName("access-token")
    var accessToken: String = "",

    @SerialName("refresh-token")
    var refreshToken: String = ""
)
