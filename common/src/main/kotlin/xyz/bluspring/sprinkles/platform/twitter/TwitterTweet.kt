package xyz.bluspring.sprinkles.platform.twitter

data class TwitterTweet(
    val id: Long,
    val content: String,
    val media: List<String>,
    val isRetweet: Boolean,
    val quotedTweet: TwitterTweet?,

    val userDisplayName: String,
    val userAvatar: String,
    val userName: String
)
