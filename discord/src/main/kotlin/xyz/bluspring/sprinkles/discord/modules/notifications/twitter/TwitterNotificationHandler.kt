package xyz.bluspring.sprinkles.discord.modules.notifications.twitter

import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.MessageCreate
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.discord.modules.notifications.NotificationHandler
import xyz.bluspring.sprinkles.platform.twitter.TwitterScraper
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class TwitterNotificationHandler : NotificationHandler("Twitter") {
    override val isEnabled: Boolean
        get() = SprinklesDiscord.instance.config.notifications.twitter.isEnabled

    private val allUsernames = SprinklesDiscord.instance.config.notifications.twitter.usernames
    val updateMessage = SprinklesDiscord.instance.config.notifications.twitter.updateMessage

    override val updateChannelIds = SprinklesDiscord.instance.config.notifications.twitter.updateChannels

    override val loopTime: Duration
        get() = 5.minutes

    override suspend fun poll() {
        for (username in allUsernames) {
            val tweets = TwitterScraper.getTweets(username)

            val previous = this.getPreviousNotifications(username)
            tweet@for (tweet in tweets) {
                val timestamp = TwitterScraper.decodeSnowflakeToTimestamp(tweet.id)

                if (System.currentTimeMillis() - timestamp >= 3.days.inWholeMilliseconds)
                    continue@tweet

                if (previous.contains(tweet.id.toString()))
                    continue@tweet

                if (tweet.isRetweet)
                    continue@tweet

                val message = MessageCreate {
                    content = updateMessage
                        .replace("%displayName%", tweet.userDisplayName)
                        .replace("%username%", username)

                    embeds += Embed {
                        title = if (tweet.isRetweet) {
                            "$username retweeted ${tweet.userDisplayName}'s tweet"
                        } else if (tweet.quotedTweet != null) {
                            "${tweet.userDisplayName} quoted ${tweet.quotedTweet!!.userDisplayName}'s tweet"
                        } else {
                            "${tweet.userDisplayName} posted a tweet"
                        }

                        description = tweet.content.take(4093).run {
                            if (this.length != tweet.content.length)
                                return@run "$this..."

                            this
                        }

                        author {
                            name = tweet.userDisplayName
                            url = "https://twitter.com/${tweet.userName}"
                            iconUrl = tweet.userAvatar
                        }

                        thumbnail = tweet.userAvatar

                        url = "https://twitter.com/${tweet.userName}/status/${tweet.id}"
                        this.timestamp = Instant.ofEpochMilli(timestamp)

                        color = 0x00aeed

                        if (tweet.media.isNotEmpty())
                            image = tweet.media.first()
                    }
                }

                for (updateChannel in updateChannels) {
                    updateChannel.sendMessage(message).queue()
                }

                markNotificationAsDone(username, tweet.id.toString())
            }
        }
    }
}