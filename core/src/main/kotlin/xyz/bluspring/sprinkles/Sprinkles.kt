package xyz.bluspring.sprinkles

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

object Sprinkles {
    private val logger = LoggerFactory.getLogger(Sprinkles::class.java)
    private val modulesToLoad: List<KClass<*>> = listOf(
        SprinklesCore::class,
        SprinklesDiscord::class,
        //SprinklesTwitch::class
    )

    val loadedModules = mutableMapOf<String, SprinklesBotModule<*>>()

    @JvmStatic
    fun main(args: Array<out String>) {
        logger.info("Starting Sprinkles...")

        runBlocking {
            for (moduleClass in modulesToLoad) {
                launch {
                    try {
                        val module = moduleClass.constructors.first().call() as SprinklesBotModule<*>

                        logger.info("Launching ${module.name} module under a new coroutine...")

                        loadedModules[module.name] = module

                        module.loadConfig()

                        logger.info("Loading ${module.name} module...")
                        module.start()
                    } catch (e: Exception) {
                        logger.error("Failed to load module ${moduleClass.jvmName}!")
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}