package xyz.bluspring.sprinkles.twitch.commands

import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent
import org.kitteh.irc.client.library.feature.twitch.messagetag.Badges
import xyz.bluspring.sprinkles.twitch.commands.custom.management.PermissionLevel
import xyz.bluspring.sprinkles.twitch.irc.TwitchIRCChat

class TwitchUser(
    val login: String,
    val sourceChannel: String,
    val event: ChannelMessageEvent
) {
    fun isModerator(): Boolean {
        return event.getTag(Badges.NAME).run {
            return@run if (this.isPresent)
                (this.get() as Badges).badges.any { it.name == "moderator" || it.name == "broadcaster" }
            else false
        }
    }

    fun matchesPermissionLevel(level: PermissionLevel): Boolean {
        return when (level) {
            PermissionLevel.USER -> true
            PermissionLevel.MODERATOR -> isModerator() || isBroadcaster()
            PermissionLevel.BROADCASTER -> isBroadcaster()
        }
    }

    fun isBroadcaster(): Boolean {
        return event.getTag(Badges.NAME).run {
            return@run if (this.isPresent)
                (this.get() as Badges).badges.any { it.name == "broadcaster" }
            else false
        }
    }

    fun send(message: String) {
        TwitchIRCChat.client?.getChannel("#$sourceChannel")?.ifPresent {
            it.sendMessage("@$login, $message")
        }
    }
}
