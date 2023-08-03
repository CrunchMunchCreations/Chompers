package xyz.bluspring.sprinkles.twitch.commands

import xyz.bluspring.sprinkles.twitch.irc.TwitchIRCChat

class TwitchUser(
    val login: String,
    val sourceChannel: String
) {
    fun send(message: String) {
        TwitchIRCChat.client?.getChannel("#$sourceChannel")?.ifPresent {
            it.sendMessage("@$login, $message")
        }
    }
}
