package xyz.bluspring.sprinkles.twitch

import xyz.bluspring.sprinkles.SprinklesBotModule
import xyz.bluspring.sprinkles.twitch.config.TwitchChatbotConfig

class SprinklesTwitch : SprinklesBotModule<TwitchChatbotConfig>(NAME) {
    override val configSerializer = TwitchChatbotConfig.serializer()
    override val dummy = TwitchChatbotConfig()

    override fun start() {
    }

    override fun stop() {
    }

    companion object {
        const val NAME = "Twitch"
    }
}