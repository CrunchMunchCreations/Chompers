package xyz.bluspring.sprinkles.twitch.commands.general

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser

class TestCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {
        context.source.send("Test complete!")

        return 1
    }
}