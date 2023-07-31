package xyz.bluspring.sprinkles.discord.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordNotifConfig(
    val usernames: List<String> = listOf(),
    @SerialName("update-channels")
    val updateChannels: List<String> = listOf()
)
