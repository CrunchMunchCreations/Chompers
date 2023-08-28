package xyz.bluspring.sprinkles.platform.twitter

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import java.net.URL
import java.net.URLDecoder

object TwitterScraper {
    // A lot easier to use this than the official Twitter, funny enough.
    const val TWITTER_URL = "https://nitter.net"
    const val TWITTER_EPOCH = 1288834974657L

    fun getTweets(username: String): List<TwitterTweet> {
        val html = URL("$TWITTER_URL/$username").readText()

        val handler = TwitterUserPageHandler(username)

        val parser = KsoupHtmlParser(handler = handler)
        parser.write(html)
        parser.end()

        return handler.tweets.sortedByDescending { it.id }
    }

    fun decodeSnowflakeToTimestamp(id: Long): Long {
        val idBinary = id.toString(2).padStart(64, '0')

        return idBinary.substring(0, 42).toLong(2) + TWITTER_EPOCH
    }

    private class TwitterUserPageHandler(val username: String) : KsoupHtmlHandler {
        val tweets = mutableListOf<TwitterTweet>()

        private var isInTimelineItem = false
        private var currentDivLevel = 0
        private var currentQuoteDivLevel = 0
        private var isInTweetContent = false
        private var isInTweetAvatar = false

        // quote
        private var isInQuoteItem = false
        private var isInQuoteContent = false

        // tweet info
        private var currentTweetId = ""
        private var currentTweetContent = ""
        private var currentMediaContent = mutableListOf<String>()
        private var currentIsRetweet = false
        private var currentUsername = username
        private var currentDisplayName = ""
        private var currentAvatar = ""

        private var quotedTweetId = ""
        private var quotedTweetContent = ""
        private var quotedMediaContent = mutableListOf<String>()
        private var quotedUsername = ""
        private var quotedDisplayName = ""

        override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
            if (name == "div" && attributes.contains("class") && attributes["class"]?.contains("timeline-item") == true) {
                isInTimelineItem = true
            }

            if (name == "div" && attributes.contains("class") && attributes["class"]?.contains("tweet-content") == true) {
                isInTweetContent = true
            }

            if (name == "div" && attributes.contains("class") && attributes["class"]?.contains("quote ") == true) {
                isInQuoteItem = true
            }

            if (name == "div" && attributes.contains("class") && attributes["class"]?.contains("quote-text") == true) {
                isInQuoteContent = true
            }

            if (name == "a" && isInTimelineItem && attributes.contains("class") && attributes["class"]?.contains("tweet-link") == true) {
                val href = attributes["href"]!!
                val tweetUsername = href.removePrefix("/").replaceAfter("/", "").replace("/", "")

                if (tweetUsername.lowercase().trim() != username.lowercase().trim()) {
                    currentIsRetweet = true
                    currentUsername = tweetUsername
                }

                val tweetId = href.removePrefix("/$tweetUsername/status/").replace(Regex("[^0-9]"), "")
                currentTweetId = tweetId
            }

            if (name == "a" && isInQuoteItem && attributes.contains("class") && attributes["class"]?.contains("quote-link") == true) {
                val href = attributes["href"]!!
                val tweetUsername = href.removePrefix("/").replaceAfter("/", "")

                quotedUsername = tweetUsername

                val tweetId = href.removePrefix("/$tweetUsername/status/").replace(Regex("[^0-9]"), "")
                quotedTweetId = tweetId
            }

            if (name == "a" && isInTimelineItem && attributes.contains("class") && attributes["class"]?.contains("tweet-avatar") == true) {
                isInTweetAvatar = true
            }

            if (name == "a" && isInTimelineItem && attributes.contains("class") && attributes["class"]?.contains("fullname") == true) {
                if (isInQuoteItem)
                    quotedDisplayName = attributes["title"] ?: ""
                else
                    currentDisplayName = attributes["title"] ?: ""
            } else if (name == "a" && isInTimelineItem && attributes.contains("class") && attributes["class"]?.contains("username") == true) {
                if (isInQuoteItem)
                    quotedUsername = attributes["title"]?.removePrefix("@") ?: ""
                else
                    currentUsername = attributes["title"]?.removePrefix("@") ?: ""
            }

            if (name == "a" && isInTimelineItem && attributes.contains("class") && attributes["class"]?.contains("still-image") == true) {
                val imgUrl = URLDecoder.decode(attributes["href"], "utf-8")
                val originalImgUrl = "https://pbs.twimg.com/media/${imgUrl.removePrefix("/pic/orig/media/").replace(".jpg", "")}?format=jpg&name=medium"

                if (!isInQuoteItem)
                    currentMediaContent.add(originalImgUrl)
                else
                    quotedMediaContent.add(originalImgUrl)
            }

            if (name == "img" && isInTweetAvatar) {
                val imgUrl = URLDecoder.decode(attributes["src"], "utf-8")
                val originalImageUrl = "https://pbs.twimg.com/${imgUrl.removePrefix("/pic/").replace("_bigger", "_400x400")}"

                currentAvatar = originalImageUrl
                isInTweetAvatar = false
            }

            if (!isInQuoteItem)
                if (name == "img" && isInTweetContent && attributes.contains("class") && attributes["class"]?.contains("ext-emoji") == true) {
                    currentTweetContent += attributes["alt"] ?: ""
                }
            else
                if (name == "img" && isInQuoteContent && attributes.contains("class") && attributes["class"]?.contains("ext-emoji") == true) {
                    quotedTweetContent += attributes["alt"] ?: ""
                }

            if (isInTimelineItem && name == "div")
                currentDivLevel++
        }

        override fun onText(text: String) {
            if (isInTweetContent) {
                currentTweetContent += " $text"
            }

            if (isInQuoteContent) {
                quotedTweetContent += " $text"
            }
        }

        override fun onCloseTag(name: String, isImplied: Boolean) {
            if (name == "div" && isInTimelineItem) {
                if (isInTweetContent)
                    isInTweetContent = false

                if (isInQuoteContent)
                    isInQuoteContent = false

                if (currentQuoteDivLevel > 0)
                    currentQuoteDivLevel--

                if (currentDivLevel > 0)
                    currentDivLevel--

                if (currentQuoteDivLevel <= 0) {
                    currentQuoteDivLevel = 0
                    isInQuoteItem = false
                }

                if (currentDivLevel <= 0) {
                    currentDivLevel = 0
                    isInTimelineItem = false

                    val tweet = TwitterTweet(
                        id = currentTweetId.toLong(),
                        content = currentTweetContent.trim(),
                        isRetweet = currentIsRetweet,
                        media = currentMediaContent.toList(),
                        quotedTweet = if (quotedTweetId.isNotBlank())
                            TwitterTweet(
                                id = quotedTweetId.toLong(),
                                content = quotedTweetContent.trim(),
                                isRetweet = false,
                                quotedTweet = null,
                                media = quotedMediaContent.toList(),
                                userName = quotedUsername,
                                userDisplayName = quotedDisplayName,
                                userAvatar = ""
                            )
                        else null,

                        userAvatar = currentAvatar,
                        userDisplayName = currentDisplayName,
                        userName = currentUsername
                    )

                    tweets.add(tweet)

                    // there's probably a better way to reset all these
                    currentTweetId = ""
                    currentTweetContent = ""
                    currentIsRetweet = false
                    currentMediaContent.clear()
                    currentUsername = username
                    currentDisplayName = ""
                    currentAvatar = ""

                    quotedTweetId = ""
                    quotedTweetContent = ""
                    quotedMediaContent.clear()
                    quotedUsername = ""
                    quotedDisplayName = ""
                }
            }
        }
    }
}