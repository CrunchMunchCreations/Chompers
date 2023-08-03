package xyz.bluspring.sprinkles.twitch.commands.custom

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser
import xyz.bluspring.sprinkles.twitch.commands.custom.management.CustomCommandManager

class CustomListCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {
        val prefix = SprinklesTwitch.instance.config.prefix

        context.source.send("Enabled custom commands: ${CustomCommandManager.commands.filter { it.enabled }.joinToString(" ") { "$prefix${it.name}" }}")
        context.source.send("Disabled custom commands: ${CustomCommandManager.commands.filter { !it.enabled }.joinToString(" ") { "$prefix${it.name}" }}")

        return 1
    }
}