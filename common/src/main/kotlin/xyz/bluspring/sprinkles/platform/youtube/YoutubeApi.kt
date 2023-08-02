package xyz.bluspring.sprinkles.platform.youtube

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.SprinklesCore
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object YoutubeApi {
    private val logger = LoggerFactory.getLogger(YoutubeApi::class.java)

    fun get(url: String): JsonObject? {
        val client = HttpClient.newHttpClient()

        val req = HttpRequest.newBuilder(URI.create("$url&key=${SprinklesCore.instance.config.api.youtube.apiKey}"))
            .apply {
                GET()
            }
            .build()

        val res = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (res.statusCode() != 200) {
            logger.error("Failed to GET $url: ${res.statusCode()} ${res.body()}")
            return null
        }

        return JsonParser.parseString(res.body()).asJsonObject
    }
}