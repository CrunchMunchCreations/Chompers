package xyz.bluspring.sprinkles.twitch.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwitchChatbotConfig(
    @SerialName("redirect-uri")
    val redirectUri: String = "http://localhost:52414",
    val keys: TwitchAuthConfig = TwitchAuthConfig()
)