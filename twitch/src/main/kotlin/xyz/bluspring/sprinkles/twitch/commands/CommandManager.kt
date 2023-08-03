package xyz.bluspring.sprinkles.twitch.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import xyz.bluspring.sprinkles.twitch.commands.custom.CustomListCommand
import xyz.bluspring.sprinkles.twitch.commands.custom.CustomToggleCommand
import xyz.bluspring.sprinkles.twitch.commands.general.CommandsListCommand
import xyz.bluspring.sprinkles.twitch.commands.general.FollowAgeCommand
import xyz.bluspring.sprinkles.twitch.commands.general.FollowAgeWithUserCommand

class CommandManager {
    val multiDispatcher = MultiCommandDispatcher<TwitchUser>()
    val dispatcher = CommandDispatcher<TwitchUser>()

    fun register(command: LiteralArgumentBuilder<TwitchUser>): LiteralCommandNode<TwitchUser> {
        return multiDispatcher.register(command, dispatcher)
    }

    init {
        register(
            literal("followage")
                .then(
                    argument("user", StringArgumentType.string())
                        .executes(FollowAgeWithUserCommand())
                )
                .executes(FollowAgeCommand())
        )

        register(
            literal("commands")
                .executes(CommandsListCommand())
        )

        register(
            literal("command")
                .requires { it.isModerator() }
                /*.then(
                    literal("add")
                        .executes(CustomAddCommand())
                )
                .then(
                    literal("remove")
                        .executes(CustomRemoveCommand())
                )*/
                .then(
                    literal("toggle")
                        .then(
                            argument("command", StringArgumentType.word())
                                .executes(CustomToggleCommand())
                        )
                )
                .then(
                    literal("list")
                        .executes(CustomListCommand())
                )
        )
    }

    companion object {
        fun literal(name: String): LiteralArgumentBuilder<TwitchUser> = LiteralArgumentBuilder.literal(name)
        fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<TwitchUser, T> = RequiredArgumentBuilder.argument(name, type)
    }
}