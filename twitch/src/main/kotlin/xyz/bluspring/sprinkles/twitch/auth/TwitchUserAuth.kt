package xyz.bluspring.sprinkles.twitch.auth

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.SprinklesCore
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch
import xyz.bluspring.sprinkles.twitch.auth.callback.TwitchCallbackServer
import xyz.bluspring.sprinkles.util.HttpHelper
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object TwitchUserAuth {
    private val logger = LoggerFactory.getLogger(TwitchUserAuth::class.java)
    private val STORAGE_FILE = File(SprinklesCore.instance.config.storage.auth, "twitch_auth.json")

    var twitchUsername: String? = null
    var displayName: String? = null

    var accessToken: String? = null
    var refreshToken: String? = null
    var expiryTimestamp: Long = 0L

    fun loadPrevious() {
        if (!STORAGE_FILE.exists()) {
            getNewAccessToken()

            return
        }
        
        val json = JsonParser.parseString(STORAGE_FILE.readText()).asJsonObject

        accessToken = json.get("access_token").asString
        refreshToken = json.get("refresh_token").asString
        expiryTimestamp = json.get("expiry_timestamp").asLong
        updateTwitchUserInfo()
    }

    fun save() {
        if (!STORAGE_FILE.exists())
            STORAGE_FILE.createNewFile()

        val json = JsonObject()

        json.addProperty("access_token", accessToken)
        json.addProperty("refresh_token", refreshToken)
        json.addProperty("expiry_timestamp", expiryTimestamp)

        STORAGE_FILE.writeText(json.toString())
    }

    fun getAuthUrl(): String {
        return "https://id.twitch.tv/oauth2/authorize?client_id=${SprinklesCore.instance.config.api.twitch.clientId}&redirect_uri=${SprinklesTwitch.instance.config.redirectUri}&response_type=code&scope=chat%3Aread+chat%3Aedit+channel%3Amoderate+moderator%3Aread%3Afollowers"
    }

    fun getNewAccessToken() {
        if (SprinklesCore.instance.config.api.twitch.clientId.isBlank())
            throw IllegalStateException("Client ID not provided!")

        if (SprinklesCore.instance.config.api.twitch.clientSecret.isBlank())
            throw IllegalStateException("Client Secret not provided!")

        // TODO: how do i use coroutines properly for this
        val thread = Thread({
            TwitchCallbackServer.start()
        }, "TwitchAuth Server")
        thread.start()

        logger.info("Log into Twitch API - ${getAuthUrl()}")
    }

    fun authorizeAccessToken(code: String) {
        if (SprinklesCore.instance.config.api.twitch.clientId.isBlank())
            throw IllegalStateException("Client ID not provided!")

        if (SprinklesCore.instance.config.api.twitch.clientSecret.isBlank())
            throw IllegalStateException("Client Secret not provided!")

        val params = mapOf(
            "client_id" to SprinklesCore.instance.config.api.twitch.clientId,
            "client_secret" to SprinklesCore.instance.config.api.twitch.clientSecret,
            "code" to code,
            "grant_type" to "authorization_code",
            "redirect_uri" to SprinklesTwitch.instance.config.redirectUri
        )

        val client = HttpClient.newHttpClient()
        val req = HttpRequest
            .newBuilder().apply {
                uri(URI.create("https://id.twitch.tv/oauth2/token"))
                POST(HttpHelper.formData(params))
                header("Content-Type", "application/x-www-form-urlencoded")
            }
            .build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw IllegalStateException("Failed to authenticate with Twitch! ${response.statusCode()} ${response.body()}")
        }

        val json = JsonParser.parseString(response.body()).asJsonObject

        accessToken = json.get("access_token").asString
        refreshToken = json.get("refresh_token").asString
        updateTwitchUserInfo()
        save()
    }

    fun refreshToken() {
        if (SprinklesCore.instance.config.api.twitch.clientId.isBlank())
            throw IllegalStateException("Client ID not provided!")

        if (SprinklesCore.instance.config.api.twitch.clientSecret.isBlank())
            throw IllegalStateException("Client Secret not provided!")

        val params = mapOf(
            "client_id" to SprinklesCore.instance.config.api.twitch.clientId,
            "client_secret" to SprinklesCore.instance.config.api.twitch.clientSecret,
            "grant_type" to "refresh_token",
            "refresh_token" to refreshToken!!
        )

        val client = HttpClient.newHttpClient()
        val req = HttpRequest
            .newBuilder().apply {
                uri(URI.create("https://id.twitch.tv/oauth2/token"))
                POST(HttpHelper.formData(params))
                header("Content-Type", "application/x-www-form-urlencoded")
            }
            .build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 401) {
            accessToken = null
            refreshToken = null
            getNewAccessToken()

            return
        } else if (response.statusCode() != 200) {
            logger.error("Failed to refresh token! ${response.statusCode()} ${response.body()}")

            return
        }

        val json = JsonParser.parseString(response.body()).asJsonObject

        accessToken = json.get("access_token").asString
        refreshToken = json.get("refresh_token").asString
        updateTwitchUserInfo()
        save()
    }

    fun updateTwitchUserInfo() {
        if (accessToken == null)
            return

        val client = HttpClient.newHttpClient()
        val req = HttpRequest.newBuilder().apply {
            uri(URI.create("https://api.twitch.tv/helix/users"))
            GET()
            header("Client-Id", SprinklesCore.instance.config.api.twitch.clientId)
            header("Authorization", "Bearer $accessToken")
        }.build()

        val response = client.send(req, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() == 401) {
            refreshToken()

            return
        }

        val json = JsonParser.parseString(response.body()).asJsonObject

        val userData = json.getAsJsonArray("data")[0].asJsonObject

        twitchUsername = userData.get("login").asString
        displayName = userData.get("display_name").asString

        SprinklesTwitch.instance.startIrc()
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

            refreshToken()
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

            refreshToken()
            return get(uri, true)
        }

        return JsonParser.parseString(resp.body()).asJsonObject
    }
}