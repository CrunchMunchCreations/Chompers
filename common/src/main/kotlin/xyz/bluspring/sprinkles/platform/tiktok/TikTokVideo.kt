package xyz.bluspring.sprinkles.platform.tiktok

data class TikTokVideo(
    val id: String,
    val description: String,
    val author: TikTokCreator,
    val thumbnail: String
)
