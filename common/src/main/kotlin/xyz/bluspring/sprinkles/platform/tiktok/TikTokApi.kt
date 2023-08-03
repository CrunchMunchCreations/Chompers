package xyz.bluspring.sprinkles.platform.tiktok

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import java.net.URL

object TikTokApi {
    private val URL_REGEX = Regex("http(s)?://(www.)?tiktok.com/@[a-zA-Z0-9_.]+/video/")

    /*fun getVideos(username: String): List<BasicTikTokVideoMetadata> {
        val html = URL("https://tiktok.com/@$username").readText()

        val handler = TikTokCreatorPageHandler()

        val parser = KsoupHtmlParser(handler = handler)
        parser.write(html)
        parser.end()

        return handler.videos
    }*/

    fun getVideos(username: String): List<TikTokVideo> {
        val html = URL("https://tiktok.com/@$username").readText()

        val handler = TikTokCreatorPageHandler()

        val parser = KsoupHtmlParser(handler = handler)
        parser.write(html)
        parser.end()

        val json = handler.jsonData ?: return listOf()

        val videos = mutableListOf<TikTokVideo>()

        val items = json.getAsJsonObject("ItemModule")

        for (id in items.keySet()) {
            val data = items.getAsJsonObject(id)

            videos.add(
                TikTokVideo(
                    id,
                    data.get("desc").asString,
                    TikTokCreator(
                        data.get("authorId").asString,
                        data.get("nickname").asString,
                        data.get("author").asString,
                        data.get("avatarThumb").asString,
                        ""
                    ),
                    data.getAsJsonObject("video").get("cover").asString
                )
            )
        }

        return videos
    }

    fun getVideoData(id: Long): TikTokVideo {
        val data = URL("https://api2.musical.ly/aweme/v1/feed/?aweme_id=$id&version_code=262&app_name=musical_ly&channel=App&device_id=null&os_version=14.4.2&device_platform=iphone&device_type=iPhone9&region=US&carrier_region=US").readText()
        val json = JsonParser.parseString(data).asJsonObject

        val videoData = json.getAsJsonArray("aweme_list")[0].asJsonObject

        val authorData = videoData.getAsJsonObject("author")
        val avatarUrl = if (authorData.has("avatar_larger")) {
            authorData.getAsJsonObject("avatar_larger")
                .getAsJsonArray("url_list")
                .first { it.asString.contains(".jpeg") }
                .asString
        } else if (authorData.has("avatar_medium")) {
            authorData.getAsJsonObject("avatar_medium")
                .getAsJsonArray("url_list")
                .first { it.asString.contains(".jpeg") }
                .asString
        } else if (authorData.has("avatar_thumb")) {
            authorData.getAsJsonObject("avatar_thumb")
                .getAsJsonArray("url_list")
                .first { it.asString.contains(".jpeg") }
                .asString
        } else ""

        return TikTokVideo(
            videoData.get("aweme_id").asString,
            videoData.get("desc").asString,
            TikTokCreator(
                authorData.get("uid").asString,
                authorData.get("nickname").asString,
                authorData.get("unique_id").asString,
                avatarUrl,
                authorData.get("signature").asString
            ),
            videoData.getAsJsonObject("video").getAsJsonObject("cover").getAsJsonArray("url_list")
                .first { it.asString.contains(".jpeg") }
                .asString
        )
    }

    private class TikTokCreatorPageHandler : KsoupHtmlHandler {
        var jsonData: JsonObject? = null
        /*private var isInVideosList = false
        private var isInVideoTag = false
        private var isInVideoMetaTag = false

        override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
            when (name) {
                "div" -> {
                    if (!attributes.contains("class"))
                        return

                    val classList = attributes["class"]!!

                    if (classList.contains("DivVideoFeedV2")) {
                        isInVideosList = true
                    } else if (classList.contains("DivItemContainerV2") && isInVideosList) {
                        isInVideoTag = true
                    }
                }

                "a" -> {
                    if (!attributes.contains("class"))
                        return

                    val classList = attributes["class"]!!

                    if (classList.contains("AMetaCaptionLine") && isInVideosList && isInVideoTag) {
                        isInVideoMetaTag = true

                        val url = attributes["href"]!!
                        val title = attributes["title"]!!
                        val id = url.replace(URL_REGEX, "").toLong()

                        val timestamp = (id shr 32) * 1000

                        val video = BasicTikTokVideoMetadata(
                            timestamp, id,
                            title, url
                        )

                        videos.add(video)
                    }
                }
            }
        }

        override fun onCloseTag(name: String, isImplied: Boolean) {
            when (name) {
                "a" -> {
                    if (isInVideoMetaTag)
                        isInVideoMetaTag = false
                }

                "div" -> {
                    if (!isInVideoMetaTag && isInVideoTag)
                        isInVideoTag = false

                    if (!isInVideoMetaTag && !isInVideoTag && !isInVideosList)
                        isInVideosList = false
                }
            }
        }*/

        private var isInScript = false
        override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
            if (name == "script" && attributes.contains("id") && attributes["id"] == "SIGI_STATE") {
                isInScript = true
            }
        }

        override fun onText(text: String) {
            if (!isInScript)
                return

            val json = JsonParser.parseString(text).asJsonObject
            jsonData = json
        }

        override fun onCloseTag(name: String, isImplied: Boolean) {
            if (name == "script" && isInScript)
                isInScript = false
        }
    }
}