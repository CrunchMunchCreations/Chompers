package xyz.bluspring.sprinkles.twitch.config

import kotlinx.serialization.Serializable

@Serializable
data class TwitchChatbotConfig(
    val keys: TwitchAuthConfig = TwitchAuthConfig()
)