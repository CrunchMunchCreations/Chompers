package xyz.bluspring.sprinkles.twitch

import xyz.bluspring.sprinkles.SprinklesBotModule
import xyz.bluspring.sprinkles.twitch.auth.TwitchUserAuth
import xyz.bluspring.sprinkles.twitch.config.TwitchChatbotConfig

class SprinklesTwitch : SprinklesBotModule<TwitchChatbotConfig>(NAME) {
    override val configSerializer = TwitchChatbotConfig.serializer()
    override val dummy = TwitchChatbotConfig()

    override fun start() {
        instance = this

        TwitchUserAuth.loadPrevious()
    }

    fun startIrc() {

    }

    override fun stop() {
    }

    companion object {
        lateinit var instance: SprinklesTwitch
        const val NAME = "Twitch"
    }
}