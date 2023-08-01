package xyz.bluspring.sprinkles

import kotlinx.serialization.KSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

abstract class SprinklesBotModule<C : Any>(val name: String) {
    lateinit var config: C

    protected val logger: Logger = LoggerFactory.getLogger("$name Module")

    abstract val configSerializer: KSerializer<C>
    abstract val dummy: C

    val configFile: File
        get() = File("config-${name.lowercase()}.yml")

    open fun loadConfig() {
        config = try {
            SprinklesCore.yaml.decodeFromString(configSerializer, configFile.readText())
        } catch (e: Exception) {
            logger.error("Failed to load config! Not going to continue.")
            e.printStackTrace()
            throw e
        }

        saveConfig()
    }

    open fun saveConfig() {
        if (!configFile.exists())
            configFile.createNewFile()

        configFile.writeText(SprinklesCore.yaml.encodeToString(configSerializer, config))
    }

    abstract fun start()
    abstract fun stop()
}