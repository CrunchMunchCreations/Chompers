package xyz.bluspring.sprinkles.discord.config

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class DiscordNotificationsConfig(
    val twitch: DiscordNotifConfig = DiscordNotifConfig(),
    @YamlComment(
        "The usernames field is actually the YouTube channel IDs, which you can get here:",
        "https://commentpicker.com/youtube-channel-id.php"
    )
    val youtube: DiscordNotifConfig = DiscordNotifConfig(),
    val tiktok: DiscordNotifConfig = DiscordNotifConfig(),
    val twitter: DiscordNotifConfig = DiscordNotifConfig()
)