package xyz.bluspring.sprinkles.config

import kotlinx.serialization.Serializable
import xyz.bluspring.sprinkles.config.platform.TwitchConfig
import xyz.bluspring.sprinkles.config.platform.YoutubeConfig

@Serializable
data class ApiConfigs(
    val twitch: TwitchConfig = TwitchConfig(),
    val youtube: YoutubeConfig = YoutubeConfig()
)
