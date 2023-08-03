package xyz.bluspring.sprinkles

import com.charleskorn.kaml.SingleLineStringStyle
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.google.gson.Gson
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
        val gson: Gson = GsonBuilder().apply {
            setLenient()
            setPrettyPrinting()
        }.create()

        val yaml = Yaml(configuration = YamlConfiguration(
            singleLineStringStyle = SingleLineStringStyle.PlainExceptAmbiguous,
            encodeDefaults = false
        ))

        const val NAME = "Core"

        lateinit var instance: SprinklesCore
    }
}