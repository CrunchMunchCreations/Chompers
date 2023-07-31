package xyz.bluspring.sprinkles

import xyz.bluspring.sprinkles.config.MainConfig

class SprinklesCore : SprinklesBotModule<MainConfig>(NAME) {
    override val configSerializer = MainConfig.serializer()
    override val dummy = MainConfig()

    override fun start() {
        instance = this


    }

    override fun stop() {

    }

    companion object {
        const val NAME = "Core"

        lateinit var instance: SprinklesCore
    }
}