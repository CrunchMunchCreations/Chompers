package xyz.bluspring.sprinkles.discord.modules.notifications.youtube

import com.google.gson.JsonObject
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.discord.modules.notifications.NotificationHandler
import xyz.bluspring.sprinkles.platform.youtube.YoutubeApi
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.time.Duration.Companion.days

class YoutubeNotificationHandler : NotificationHandler("YouTube") {
    override val isEnabled: Boolean
        get() = SprinklesDiscord.instance.config.notifications.youtube.isEnabled

    private val allIds = SprinklesDiscord.instance.config.notifications.youtube.usernames
    val updateMessage = SprinklesDiscord.instance.config.notifications.youtube.updateMessage

    override val updateChannelIds = SprinklesDiscord.instance.config.notifications.youtube.updateChannels

    override suspend fun poll() {
        for (id in allIds) {
            val uploads = YoutubeApi.get("https://www.googleapis.com/youtube/v3/playlistItems?playlistId=UU${id.removePrefix("UC")}&part=snippet&maxResults=20")

            if (uploads == null) {
                logger.error("Failed to get uploads for channel ID $id!")
                continue
            }

            val previous = getPreviousNotifications(id)

            item@for (itemElement in uploads.getAsJsonArray("items")) {
                val item = itemElement.asJsonObject

                val snippet = item.getAsJsonObject("snippet")
                val videoId = snippet.getAsJsonObject("resourceId").get("videoId").asString

                if (previous.contains(videoId))
                    continue@item

                val temporal = DateTimeFormatter.ISO_INSTANT.parse(snippet.get("publishedAt").asString)
                val publishedAt = Instant.from(temporal)
                val publishedMs = publishedAt.toEpochMilli()

                if (System.currentTimeMillis() - publishedMs > 3.days.inWholeMilliseconds)
                    continue@item

                val title = snippet.get("title").asString
                val description = snippet.get("description").asString

                val thumbnails = snippet.getAsJsonObject("thumbnails")
                val thumbnailUrl = getHighestResolutionThumbnail(thumbnails)

                val channelTitle = snippet.get("videoOwnerChannelTitle").asString
                val channelThumbnail = getChannelProfilePicture(id)

                val message = MessageCreate {
                    content = updateMessage
                        .replace("%displayName%", channelTitle)
                        .replace("%title%", title)

                    embeds += Embed {
                        this.title = title
                        this.url = "https://youtube.com/watch?v=$videoId"

                        author {
                            name = "$channelTitle has uploaded on YouTube!"
                            url = "https://youtube.com/channel/$id"
                            iconUrl = channelThumbnail
                        }

                        image = thumbnailUrl
                        timestamp = temporal

                        field {
                            name = "Description"
                            value = description.replaceAfter("\n", "").take(255)
                        }

                        color = 0xFF0000
                    }
                }

                updateChannels.forEach { channel ->
                    channel.sendMessage(message).queue()
                }

                markNotificationAsDone(id, videoId)
            }
        }
    }

    private fun getHighestResolutionThumbnail(thumbnails: JsonObject): String {
        return if (thumbnails.has("maxres"))
            thumbnails.getAsJsonObject("maxres").get("url").asString
        else if (thumbnails.has("standard"))
            thumbnails.getAsJsonObject("standard").get("url").asString
        else if (thumbnails.has("high"))
            thumbnails.getAsJsonObject("high").get("url").asString
        else if (thumbnails.has("medium"))
            thumbnails.getAsJsonObject("medium").get("url").asString
        else if (thumbnails.has("default"))
            thumbnails.getAsJsonObject("default").get("url").asString
        else ""
    }

    private fun getChannelProfilePicture(channel: String): String {
        val json = YoutubeApi.get("https://www.googleapis.com/youtube/v3/channels?part=snippet&id=$channel") ?: return ""

        for (item in json.getAsJsonArray("items")) {
            try {
                val data = item.asJsonObject

                val images = data.getAsJsonObject("snippet").getAsJsonObject("thumbnails")

                return getHighestResolutionThumbnail(images)
            } catch (_: Exception) {}
        }

        return ""
    }
}