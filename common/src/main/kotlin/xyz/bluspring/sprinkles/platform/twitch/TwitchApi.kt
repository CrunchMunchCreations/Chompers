package xyz.bluspring.sprinkles.platform.twitch

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.SprinklesCore
import xyz.bluspring.sprinkles.util.HttpHelper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object TwitchApi {
    private val logger = LoggerFactory.getLogger(TwitchApi::class.java)

    var accessToken: String? = null

    fun refreshAccessToken() {
        if (SprinklesCore.instance.config.api.twitch.clientId.isBlank())
            throw IllegalStateException("Client ID not provided!")

        if (SprinklesCore.instance.config.api.twitch.clientSecret.isBlank())
            throw IllegalStateException("Client Secret not provided!")

        val client = HttpClient.newHttpClient()

        val params = mapOf(
            "client_id" to SprinklesCore.instance.config.api.twitch.clientId,
            "client_secret" to SprinklesCore.instance.config.api.twitch.clientSecret,
            "grant_type" to "client_credentials"
        )

        val req = HttpRequest
            .newBuilder(URI.create("https://id.twitch.tv/oauth2/token"))
            .apply {
                POST(HttpHelper.formData(params))
                header("Content-Type", "application/x-www-form-urlencoded")
            }
            .build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 401) {
            accessToken = null

            return
        } else if (response.statusCode() != 200) {
            logger.error("Failed to refresh token! ${response.statusCode()} ${response.body()}")

            return
        }

        val json = JsonParser.parseString(response.body()).asJsonObject

        accessToken = json.get("access_token").asString
        logger.info("Successfully updated Twitch API access token!")
    }

    fun post(uri: URI, json: JsonObject, secondTry: Boolean = false): JsonObject? {
        val client = HttpClient.newHttpClient()
        val req = HttpRequest.newBuilder(uri)
            .apply {
                POST(HttpRequest.BodyPublishers.ofString(json.toString()))

                header("Client-Id", SprinklesCore.instance.config.api.twitch.clientId)
                header("Authorization", "Bearer $accessToken")
            }
            .build()

        val resp = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (resp.statusCode() == 401) {
            if (secondTry) {
                logger.error("Failed to POST $uri - ${resp.statusCode()} ${resp.body()}")
                return null
            }

            refreshAccessToken()
            return post(uri, json, true)
        }

        return JsonParser.parseString(resp.body()).asJsonObject
    }

    fun get(uri: URI, secondTry: Boolean = false): JsonObject? {
        val client = HttpClient.newHttpClient()
        val req = HttpRequest.newBuilder(uri)
            .apply {
                GET()

                header("Client-Id", SprinklesCore.instance.config.api.twitch.clientId)
                header("Authorization", "Bearer $accessToken")
            }
            .build()

        val resp = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (resp.statusCode() == 401) {
            if (secondTry) {
                logger.error("Failed to POST $uri - ${resp.statusCode()} ${resp.body()}")
                return null
            }

            refreshAccessToken()
            return get(uri, true)
        }

        return JsonParser.parseString(resp.body()).asJsonObject
    }

    fun getUserIds(usernames: List<String>): Map<String, String> {
        val map = mutableMapOf<String, String>()

        for (usernameList in usernames.chunked(100)) {
            val json = get(URI.create("https://api.twitch.tv/helix/users?login=${usernameList.joinToString("&login=")}")) ?: continue

            if (!json.has("data")) {
                logger.error("Failed to load users: $json")
                continue
            }

            for (jsonElement in json.getAsJsonArray("data")) {
                val data = jsonElement.asJsonObject

                map[data.get("login").asString] = data.get("id").asString
            }
        }

        return map
    }
}