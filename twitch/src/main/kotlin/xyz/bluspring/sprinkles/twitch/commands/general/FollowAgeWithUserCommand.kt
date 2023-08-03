package xyz.bluspring.sprinkles.twitch.commands.general

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser

class FollowAgeWithUserCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {
        val login = StringArgumentType.getString(context, "user")
        return FollowAgeManager.sendFollowAge(context, login)
    }
}