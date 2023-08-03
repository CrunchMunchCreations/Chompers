package xyz.bluspring.sprinkles.twitch.commands.custom

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser

class CustomAddCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {
        val name = StringArgumentType.getString(context, "name")

        return 1
    }
}