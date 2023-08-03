package xyz.bluspring.sprinkles.twitch.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch

class MultiCommandDispatcher<S> {
    private val commandMappings = mutableMapOf<String, CommandDispatcher<S>>()

    fun hasCommand(name: String): Boolean {
        return commandMappings.containsKey(name)
    }

    fun register(builder: LiteralArgumentBuilder<S>, dispatcher: CommandDispatcher<S>): LiteralCommandNode<S> {
        if (commandMappings.containsKey(builder.literal) && commandMappings[builder.literal] == SprinklesTwitch.instance.commandManager.dispatcher) {
            throw IllegalArgumentException("Command is already registered!")
        }

        val node = dispatcher.register(builder)
        commandMappings[builder.literal] = dispatcher
        return node
    }

    fun execute(input: String, source: S): Int {
        val root = input.split(" ")[0]
        val dispatcher = commandMappings[root] ?: throw CommandNotFoundException()

        if (!dispatcher.root.getChild(root).canUse(source))
            throw CommandNotFoundException()

        return dispatcher.execute(input, source)
    }
}