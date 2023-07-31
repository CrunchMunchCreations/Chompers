package xyz.bluspring.sprinkles.discord

import xyz.bluspring.sprinkles.SprinklesBotModule
import xyz.bluspring.sprinkles.discord.config.DiscordConfig

class SprinklesDiscord : SprinklesBotModule<DiscordConfig>(NAME) {
    override val configSerializer = DiscordConfig.serializer()
    override val dummy = DiscordConfig()

    override fun start() {

    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    companion object {
        const val NAME = "Discord"
    }
}