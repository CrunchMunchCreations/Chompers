package xyz.bluspring.sprinkles.discord.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordConfig(
    val token: String = "",
    val notifications: DiscordNotificationsConfig = DiscordNotificationsConfig(),

    @SerialName("test-guilds")
    val testGuilds: List<Long> = listOf(),

    val admins: List<Long> = listOf()
)
