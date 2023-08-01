package xyz.bluspring.sprinkles

import com.google.gson.GsonBuilder
import xyz.bluspring.sprinkles.config.MainConfig
import xyz.bluspring.sprinkles.platform.twitch.TwitchApi

class SprinklesCore : SprinklesBotModule<MainConfig>(NAME) {
    override val configSerializer = MainConfig.serializer()
    override val dummy = MainConfig()

    override fun start() {
        instance = this

        TwitchApi.refreshAccessToken()
    }

    override fun stop() {

    }

    companion object {
        val gson = GsonBuilder().apply {
            setPrettyPrinting()
        }.create()
        const val NAME = "Core"

        lateinit var instance: SprinklesCore
    }
}