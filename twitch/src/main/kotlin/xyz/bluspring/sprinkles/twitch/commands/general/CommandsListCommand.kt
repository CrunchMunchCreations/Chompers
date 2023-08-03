package xyz.bluspring.sprinkles.twitch.commands.general

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser
import xyz.bluspring.sprinkles.twitch.commands.custom.management.CustomCommandManager

class CommandsListCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {
        val prefix = SprinklesTwitch.instance.config.prefix
        val default = SprinklesTwitch.instance.commandManager.dispatcher.root.children.filter { it.canUse(context.source) }.map { "$prefix${it.name}" }
        val custom = CustomCommandManager.commands.filter { context.source.matchesPermissionLevel(it.permissionLevel) }.map { "$prefix${it.name}" }

        context.source.send("*Default*: ${default.joinToString(" ")}, *Custom*: ${custom.joinToString(" ")}")

        return 1
    }
}