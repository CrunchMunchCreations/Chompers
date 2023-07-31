package xyz.bluspring.sprinkles.config.platform

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class YoutubeConfig(
    @SerialName("api-key")
    val apiKey: String = ""
)
