package xyz.bluspring.sprinkles.discord.config

import kotlinx.serialization.Serializable

@Serializable
data class DiscordNotificationsConfig(
    val twitch: DiscordNotifConfig = DiscordNotifConfig(),
    val youtube: DiscordNotifConfig = DiscordNotifConfig(),
    val tiktok: DiscordNotifConfig = DiscordNotifConfig()
)