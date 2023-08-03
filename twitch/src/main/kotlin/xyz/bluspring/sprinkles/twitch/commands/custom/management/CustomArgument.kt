package xyz.bluspring.sprinkles.twitch.commands.custom.management

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CustomArgument {
    /**
     * Runs a simple GET request and makes the provided
     * argument the response body.
     */
    @SerialName("custom-api")
    @Serializable
    data class CustomApiArgument(
        val url: String
    ) : CustomArgument()

    @SerialName("twitch-get")
    @Serializable
    data class TwitchGetRequest(
        val url: String,
        val useUserAuth: Boolean = false,
        val properties: List<String>
    ) : CustomArgument()
}
