package xyz.bluspring.sprinkles.twitch.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import xyz.bluspring.sprinkles.twitch.commands.general.FollowAgeCommand
import xyz.bluspring.sprinkles.twitch.commands.general.FollowAgeWithUserCommand
import xyz.bluspring.sprinkles.twitch.commands.general.TestCommand

class CommandManager {
    val dispatcher = CommandDispatcher<TwitchUser>()

    fun literal(name: String): LiteralArgumentBuilder<TwitchUser> = LiteralArgumentBuilder.literal(name)
    fun <T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<TwitchUser, T> = RequiredArgumentBuilder.argument(name, type)

    init {
        dispatcher.register(
            literal("followage")
                .then(
                    argument("user", StringArgumentType.string())
                        .executes(FollowAgeWithUserCommand())
                )
                .executes(FollowAgeCommand())
        )

        dispatcher.register(
            literal("test")
                .executes(TestCommand())
        )
    }
}