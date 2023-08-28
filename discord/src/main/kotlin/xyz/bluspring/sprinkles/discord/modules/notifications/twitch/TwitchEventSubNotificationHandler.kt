package xyz.bluspring.sprinkles.discord.modules.notifications.twitch

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.platform.twitch.TwitchApi
import java.net.URI

class TwitchEventSubNotificationHandler : TwitchNotificationHandler() {
    override val isEnabled: Boolean
        get() = SprinklesDiscord.instance.config.notifications.twitch.isEnabled

    lateinit var userIds: Map<String, String>
    lateinit var socket: WebSocket

    override fun runTimer() {
        // Prevent timer from starting
    }

    override suspend fun onEnable() {
        super.onEnable()

        if (!isEnabled)
            return

        val req = Request.Builder().apply {
            url(EVENT_SUB_URL)
        }.build()

        userIds = TwitchApi.getUserIds(SprinklesDiscord.instance.config.notifications.twitch.usernames)

        socket = OkHttpClient()
            .newWebSocket(req, object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    runBlocking {
                        val json = JsonParser.parseString(text).asJsonObject

                        val metadata = json.getAsJsonObject("metadata")
                        val payload = json.getAsJsonObject("payload")

                        when (metadata.get("message_type").asString) {
                            "session_welcome" -> {
                                val sessionId = payload.getAsJsonObject("session").get("id").asString

                                for (userId in userIds.values) {
                                    TwitchApi.post(
                                        URI.create("https://api.twitch.tv/helix/eventsub/subscriptions"),
                                        JsonObject().apply {
                                            this.addProperty("type", "stream.online")
                                            this.addProperty("version", "1")
                                            this.add("condition", JsonObject().apply {
                                                addProperty("broadcaster_user_id", userId)
                                            })
                                            this.add("transport", JsonObject().apply {
                                                addProperty("method", "websocket")
                                                addProperty("session_id", sessionId)
                                            })
                                        }
                                    ) ?: continue

                                    TwitchApi.post(
                                        URI.create("https://api.twitch.tv/helix/eventsub/subscriptions"),
                                        JsonObject().apply {
                                            this.addProperty("type", "stream.offline")
                                            this.addProperty("version", "1")
                                            this.add("condition", JsonObject().apply {
                                                addProperty("broadcaster_user_id", userId)
                                            })
                                            this.add("transport", JsonObject().apply {
                                                addProperty("method", "websocket")
                                                addProperty("session_id", sessionId)
                                            })
                                        }
                                    ) ?: continue

                                    logger.info("Added notification listener for $userId")
                                }
                            }

                            "notification" -> {
                                val subscription = payload.getAsJsonObject("subscription")

                                when (subscription.get("type").asString) {
                                    "stream.online" -> {
                                        logger.info(text)
                                        poll()
                                    }

                                    "stream.offline" -> {
                                        logger.info(text)
                                        poll()
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    companion object {
        val EVENT_SUB_URL = "wss://eventsub.wss.twitch.tv/ws"
    }
}