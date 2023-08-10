package xyz.bluspring.sprinkles.twitch.irc

import net.engio.mbassy.listener.Handler
import org.kitteh.irc.client.library.Client
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.client.library.event.connection.ClientConnectionEndedEvent
import org.kitteh.irc.client.library.event.connection.ClientConnectionEstablishedEvent
import org.kitteh.irc.client.library.feature.twitch.TwitchSupport
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch
import xyz.bluspring.sprinkles.twitch.auth.TwitchUserAuth
import xyz.bluspring.sprinkles.twitch.commands.CommandNotFoundException
import xyz.bluspring.sprinkles.twitch.commands.CooldownManager
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser
import java.util.*

object TwitchIRCChat {
    var client: Client? = null
        private set;

    private val logger = LoggerFactory.getLogger(TwitchIRCChat::class.java)

    fun startIrc() {
        if (this.client != null) {
            this.client!!.shutdown("Reconnecting under different user")
        }

        // attempt to force a refresh of the access token if needed
        TwitchUserAuth.updateTwitchUserInfo()

        if (TwitchUserAuth.twitchUsername == null || TwitchUserAuth.accessToken == null) {
            logger.error("Failed to start IRC: Username or access token is missing!")
            return
        }

        logger.info("Connecting to Twitch IRC server...")

        val client = Client.builder().apply {
            server().apply {
                host("irc.chat.twitch.tv")
                port(6697, Client.Builder.Server.SecurityType.SECURE)
                password("oauth:${TwitchUserAuth.accessToken}")
            }

            nick(TwitchUserAuth.twitchUsername!!)
        }.build()

        TwitchSupport.addSupport(client)
        client.connect()

        client.eventManager.registerEventListener(TwitchIRCListener())
        client.addChannel(*SprinklesTwitch.instance.config.channels.map { "#$it" }.toTypedArray())

        logger.info("Connected to Twitch chats for ${SprinklesTwitch.instance.config.channels.joinToString(", ")}")

        this.client = client
    }

    fun stopIrc() {
        this.client?.shutdown("Manually stopped")
        logger.info("IRC channel has been shut down.")
        this.client = null
    }

    class TwitchIRCListener {
        @Handler
        fun onChannelMessage(ev: ChannelMessageEvent) {
            if (!ev.message.startsWith(SprinklesTwitch.instance.config.prefix))
                return

            val command = ev.message.removePrefix(SprinklesTwitch.instance.config.prefix)
            val user = TwitchUser(ev.actor.nick, ev.channel.name.removePrefix("#"), ev)

            try {
                val root = command.split(" ")[0]
                if (SprinklesTwitch.instance.commandManager.multiDispatcher.execute(command, user) > 0)
                    CooldownManager.triggerCooldown(user.login, root)
            } catch (_: CommandNotFoundException) {
                user.send("Sprinkles is just a cat. Sprinkles doesn't know what you want.")
                // don't handle CommandNotFoundExceptions
            } catch (e: Exception) {
                user.send("Failed to run command! Error: ${e.message ?: e.localizedMessage}")

                logger.error("Failed to run command \"$command\"")
                e.printStackTrace()
            }
        }

        @Handler
        fun onClientConnect(ev: ClientConnectionEstablishedEvent) {
            logger.info("Established connection with Twitch IRC server!")
        }

        @Handler
        fun onClientDisconnect(ev: ClientConnectionEndedEvent) {
            if (ev.cause.isPresent) {
                ev.setAttemptReconnect(true)
                ev.reconnectionDelay = 5000
                ev.cause.get().printStackTrace()
                logger.warn("Disconnected, attempting to reconnect...")
            } else {
                ev.setAttemptReconnect(false)
                logger.warn("IRC channel stopped with no exception, attempting reconnection under a different client...")

                stopIrc()

                Timer().schedule(object : TimerTask() {
                    override fun run() {
                        startIrc()
                    }
                }, 5000L)
            }
        }
    }
}