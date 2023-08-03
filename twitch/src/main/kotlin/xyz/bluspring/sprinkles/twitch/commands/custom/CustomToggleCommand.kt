package xyz.bluspring.sprinkles.twitch.commands.custom

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser
import xyz.bluspring.sprinkles.twitch.commands.custom.management.CustomCommandManager

class CustomToggleCommand : Command<TwitchUser> {
    override fun run(context: CommandContext<TwitchUser>): Int {
        val commandId = StringArgumentType.getString(context, "command")

        val command = CustomCommandManager.commands.firstOrNull { it.name == commandId }

        if (command == null) {
            context.source.send("Command $commandId doesn't exist!")
            return 0
        }

        command.enabled = !command.enabled
        CustomCommandManager.save()

        context.source.send("Command $commandId has been ${if (command.enabled) "enabled" else "disabled"}!")

        return 1
    }
}