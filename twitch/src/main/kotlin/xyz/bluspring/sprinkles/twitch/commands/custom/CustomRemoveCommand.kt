package xyz.bluspring.sprinkles.twitch.commands.custom

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser

class CustomRemoveCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {

        return 1
    }
}