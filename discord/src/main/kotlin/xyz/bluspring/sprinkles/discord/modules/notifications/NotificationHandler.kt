package xyz.bluspring.sprinkles.discord.modules.notifications

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import xyz.artrinix.aviation.entities.AbstractModule
import xyz.bluspring.sprinkles.SprinklesCore
import java.io.File
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

abstract class NotificationHandler(val platform: String) : AbstractModule {
    private lateinit var task: TimerTask
    protected val logger = LoggerFactory.getLogger("$platform Notification Handler")

    open val loopTime: Duration = 5.minutes
    open val isEnabled = true

    override suspend fun onEnable() {
        if (!isEnabled) {
            logger.info("Notification handler module for platform $platform is currently disabled!")
            return
        }

        logger.info("Enabled notification handler module for platform $platform")

        loadMarkedNotifications()

        timer.scheduleAtFixedRate(object : TimerTask() {
            init {
                task = this
            }

            override fun run() {
                poll()
            }
        }, 0L, loopTime.inWholeMilliseconds)
    }

    override suspend fun onDisable() {
        logger.info("Disabled notification handler module for platform $platform")
        task.cancel()
    }

    protected val notificationStorage = File(SprinklesCore.instance.config.storage.notifications, "notifications_$platform.yml")
    private val markedNotifications = mutableListOf<MarkedNotification>()

    open fun getPreviousNotifications(username: String): List<String> {
        return markedNotifications.filter { it.username == username }.map { it.representation }
    }

    open fun markNotificationAsDone(username: String, representation: String) {
        markedNotifications.add(MarkedNotification(username, representation, System.currentTimeMillis()))
        clearOldNotifications()
        saveMarkedNotifications()
    }

    open fun clearOldNotifications() {
        if (markedNotifications.removeIf { (System.currentTimeMillis() - it.timestamp) >= 3.days.inWholeMilliseconds }) {
            saveMarkedNotifications()
        }
    }

    open fun loadMarkedNotifications() {
        if (!notificationStorage.exists())
            return

        val notifs = Yaml.default.decodeFromString(MarkedNotifications.serializer(), notificationStorage.readText())

        markedNotifications.clear()
        markedNotifications.addAll(notifs.notifications)
    }

    open fun saveMarkedNotifications() {
        if (!notificationStorage.exists())
            notificationStorage.createNewFile()

        notificationStorage.writeText(Yaml.default.encodeToString(MarkedNotifications.serializer(), MarkedNotifications(markedNotifications)))
    }

    abstract fun poll()

    @Serializable
    private data class MarkedNotifications(
        val notifications: List<MarkedNotification>
    )

    @Serializable
    private data class MarkedNotification(
        val username: String,
        val representation: String,
        val timestamp: Long
    )

    companion object {
        protected val logger: Logger = LoggerFactory.getLogger("Notification Handler")
        private val timer = Timer("Notification Handler")
    }
}