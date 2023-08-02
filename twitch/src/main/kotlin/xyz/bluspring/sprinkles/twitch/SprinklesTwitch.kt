package xyz.bluspring.sprinkles.twitch

import xyz.bluspring.sprinkles.SprinklesBotModule
import xyz.bluspring.sprinkles.twitch.auth.TwitchUserAuth
import xyz.bluspring.sprinkles.twitch.commands.CommandManager
import xyz.bluspring.sprinkles.twitch.config.TwitchChatbotConfig
import xyz.bluspring.sprinkles.twitch.irc.TwitchIRCChat

class SprinklesTwitch : SprinklesBotModule<TwitchChatbotConfig>(NAME) {
    override val configSerializer = TwitchChatbotConfig.serializer()
    override val dummy = TwitchChatbotConfig()

    val commandManager = CommandManager()

    override fun start() {
        instance = this

        TwitchUserAuth.loadPrevious()
    }

    fun startIrc() {
        TwitchIRCChat.startIrc()
    }

    override fun stop() {
        TwitchIRCChat.stopIrc()
    }

    companion object {
        lateinit var instance: SprinklesTwitch
        const val NAME = "Twitch"
    }
}