package xyz.bluspring.sprinkles.discord.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfig(
    val token: String = "",
    val notifications: DiscordNotificationsConfig = DiscordNotificationsConfig()
)
