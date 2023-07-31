package xyz.bluspring.sprinkles.config.platform

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchConfig(
    @SerialName("client-id")
    val clientId: String = "",

    @SerialName("client-secret")
    val clientSecret: String = ""
)