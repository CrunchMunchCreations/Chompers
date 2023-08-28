package xyz.bluspring.sprinkles.discord.modules.notifications.twitch

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.flow.MutableSharedFlow
import xyz.artrinix.aviation.events.Event
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.discord.events.TwitchStartBroadcastingEvent
import xyz.bluspring.sprinkles.discord.events.TwitchStopBroadcastingEvent
import xyz.bluspring.sprinkles.discord.modules.notifications.NotificationHandler
import xyz.bluspring.sprinkles.platform.twitch.TwitchApi
import java.net.URI
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

open class TwitchNotificationHandler : NotificationHandler("Twitch") {
    val updateMessage = SprinklesDiscord.instance.config.notifications.twitch.updateMessage

    val isLive = mutableSetOf<String>()

    override val loopTime: Duration
        get() = 2.minutes

    override val isEnabled: Boolean = true

    override val updateChannelIds = SprinklesDiscord.instance.config.notifications.twitch.updateChannels

    override suspend fun poll() {
        val allUsernames = SprinklesDiscord.instance.config.notifications.twitch.usernames

        for (usernames in allUsernames.chunked(100)) {
            val req = TwitchApi.get(URI.create("https://api.twitch.tv/helix/streams?type=live&user_login=${usernames.joinToString("&user_login=")}"))

            if (req == null) {
                logger.error("Failed to poll notifications!")
                continue
            }

            val processed = mutableListOf<String>()

            for (data in req.getAsJsonArray("data")) {
                val stream = data.asJsonObject
                val username = stream.get("user_login").asString

                if (processed.contains(username))
                    continue

                processed.add(username)

                if (!isLive.contains(username)) {
                    isLive.add(username)
                    (SprinklesDiscord.instance.aviation.events as MutableSharedFlow<Event>)
                        .emit(TwitchStartBroadcastingEvent(username))
                }

                val previous = getPreviousNotifications(username)
                if (previous.contains(stream.get("id").asString))
                    continue

                val message = MessageCreate {
                    content = updateMessage
                        .replace("%displayName%", stream.get("user_name").asString)
                        .replace("%login%", username)
                        .replace("%title%", stream.get("title").asString)
                        .replace("%game%", stream.get("game_name").asString)

                    embeds += Embed {
                        title = stream.get("title").asString
                        url = "https://twitch.tv/${username}"

                        author {
                            name = "${stream.get("user_name").asString} is now live on Twitch!"
                            url = this@Embed.url
                            iconUrl = TwitchApi.get(URI.create("https://api.twitch.tv/helix/users?login=$username"))!!.getAsJsonArray("data")[0].asJsonObject.get("profile_image_url").asString
                        }

                        image = stream.get("thumbnail_url").asString.replace("{width}", "1280").replace("{height}", "720")
                        timestamp = DateTimeFormatter.ISO_INSTANT.parse(stream.get("started_at").asString)

                        field {
                            name = "Game"
                            value = stream.get("game_name").asString
                        }

                        color = 0x6441a5
                    }
                }

                updateChannels.forEach { channel ->
                    channel.sendMessage(message).queue()
                }

                markNotificationAsDone(username, stream.get("id").asString)
            }

            if (processed.size != isLive.size) {
                val toRemove = mutableListOf<String>()

                for (username in isLive) {
                    if (processed.contains(username))
                        continue

                    toRemove.add(username)
                    (SprinklesDiscord.instance.aviation.events as MutableSharedFlow<Event>)
                        .emit(TwitchStopBroadcastingEvent(username))
                }

                isLive.removeAll(toRemove.toSet())
            }
        }
    }
}