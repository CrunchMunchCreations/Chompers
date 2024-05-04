package xyz.bluspring.sprinkles.twitch.commands.custom.management

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import xyz.bluspring.sprinkles.SprinklesCore
import xyz.bluspring.sprinkles.twitch.SprinklesTwitch
import xyz.bluspring.sprinkles.twitch.commands.CommandManager
import xyz.bluspring.sprinkles.twitch.commands.CooldownManager
import xyz.bluspring.sprinkles.twitch.commands.TwitchUser
import java.io.File
import java.net.URL

object CustomCommandManager {
    private val logger = LoggerFactory.getLogger(CustomCommandManager::class.java)

    val commands = mutableListOf<CustomCommandDefinition>()
    private val commandsFile = File(SprinklesCore.instance.config.storage.management, "commands.yml")
    var dispatcher = CommandDispatcher<TwitchUser>()

    fun reloadDispatcher() {
        logger.info("Reloading custom command dispatcher...")
        dispatcher = CommandDispatcher()
        registerAll()
        logger.info("Reloaded custom command dispatcher!")
    }

    fun load() {
        if (!commandsFile.exists())
            return

        commands.addAll(SprinklesCore.yaml.decodeFromString(CustomCommandDefinitions.serializer(), commandsFile.readText()).commands)
    }

    fun save() {
        if (!commandsFile.exists())
            commandsFile.createNewFile()

        commandsFile.writeText(SprinklesCore.yaml.encodeToString(CustomCommandDefinitions.serializer(), CustomCommandDefinitions(commands)))
    }

    fun register(command: LiteralArgumentBuilder<TwitchUser>): LiteralCommandNode<TwitchUser> {
        return SprinklesTwitch.instance.commandManager.multiDispatcher.register(command, dispatcher)
    }

    fun registerAll() {
        for (command in commands) {
            val literal = CommandManager.literal(command.name)
            literal.requires {
                command.enabled &&
                it.matchesPermissionLevel(command.permissionLevel)
            }

            var current = literal
            var currentArg: ArgumentBuilder<TwitchUser, *>? = null

            for ((name, argType) in command.args) {
                if (command.optionalArgs.contains(name) && currentArg != null) {
                    currentArg = currentArg.executes {
                        if (!CooldownManager.isWithinCooldown(it.source.login, command.name, command.globalCooldown, command.userCooldown))
                            return@executes 0

                        it.source.send(
                            String.format(
                                command.response,
                                *getArguments(it, command.args, command.optionalArgs).toTypedArray(),
                                *getCustomArguments(it, command.custom).toTypedArray()
                            ).replace("%user%", it.source.login)
                        )
                        return@executes 1
                    } as ArgumentBuilder<TwitchUser, *>
                }

                val arg = CommandManager.argument(name,
                    when (argType) {
                        BrigadierArgument.BOOLEAN -> BoolArgumentType.bool()
                        BrigadierArgument.FLOAT -> FloatArgumentType.floatArg()
                        BrigadierArgument.DOUBLE -> DoubleArgumentType.doubleArg()
                        BrigadierArgument.INTEGER -> IntegerArgumentType.integer()
                        BrigadierArgument.LONG -> LongArgumentType.longArg()
                        BrigadierArgument.STRING -> StringArgumentType.string()
                        BrigadierArgument.WORD -> StringArgumentType.word()
                        BrigadierArgument.GREEDY_STRING -> StringArgumentType.greedyString()
                    }
                )

                currentArg = if (currentArg == null)
                    arg
                else
                    currentArg.then(arg) as ArgumentBuilder<TwitchUser, *>
            }

            val aaa: MutableList<ArgumentBuilder<TwitchUser, *>> = mutableListOf(current)
            if (currentArg != null)
                aaa.add(currentArg)

            for (builder in aaa) {
                builder.executes {
                    if (!CooldownManager.isWithinCooldown(it.source.login, command.name, command.globalCooldown, command.userCooldown))
                        return@executes 0

                    it.source.send(
                        String.format(
                            command.response,
                            *getArguments(it, command.args, command.optionalArgs).toTypedArray(),
                            *getCustomArguments(it, command.custom).toTypedArray()
                        ).replace("%user%", it.source.login)
                    )
                    return@executes 1
                }
            }

            if (currentArg != null) {
                current = current.then(currentArg)
            }

            register(current)
        }
    }

    fun getCustomArguments(context: CommandContext<TwitchUser>, args: List<CustomArgument>): List<String> {
        val results = mutableListOf<String>()

        for (custom in args) {
            results.add(when (custom::class) {
                CustomArgument.CustomApiArgument::class -> {
                    val api = custom as CustomArgument.CustomApiArgument

                    val url = URL(api.url)
                    url.readText()
                }

                else -> continue
            })
        }

        return results
    }

    fun getArguments(context: CommandContext<TwitchUser>, args: Map<String, BrigadierArgument>, optional: List<String>): List<Any> {
        val results = mutableListOf<Any>()

        for ((name, argType) in args) {
            try {
                results.add(
                    when (argType) {
                        BrigadierArgument.BOOLEAN -> BoolArgumentType.getBool(context, name)
                        BrigadierArgument.FLOAT -> FloatArgumentType.getFloat(context, name)
                        BrigadierArgument.DOUBLE -> DoubleArgumentType.getDouble(context, name)
                        BrigadierArgument.INTEGER -> IntegerArgumentType.getInteger(context, name)
                        BrigadierArgument.LONG -> LongArgumentType.getLong(context, name)

                        BrigadierArgument.STRING, BrigadierArgument.WORD,
                        BrigadierArgument.GREEDY_STRING -> StringArgumentType.getString(context, name)
                    }
                )
            } catch (e: Exception) {
                if (!optional.contains(name))
                    throw e
            }
        }

        return results
    }

    @Serializable
    private data class CustomCommandDefinitions(
        val commands: List<CustomCommandDefinition>
    )
}