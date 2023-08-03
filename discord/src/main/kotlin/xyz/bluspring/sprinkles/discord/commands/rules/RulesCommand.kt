package xyz.bluspring.sprinkles.discord.commands.rules

import com.charleskorn.kaml.Yaml
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import xyz.artrinix.aviation.annotations.Name
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.Description
import xyz.artrinix.aviation.command.slash.annotations.SlashCommand
import xyz.artrinix.aviation.command.slash.annotations.SlashSubCommand
import xyz.artrinix.aviation.command.slash.annotations.SubCommandHolder
import xyz.artrinix.aviation.entities.Scaffold
import xyz.bluspring.sprinkles.SprinklesCore
import java.io.File

@SlashCommand("rules", "Manages rules", defaultUserPermissions = [ Permission.MANAGE_SERVER ])
class RulesCommand : Scaffold {
    // rules category create <id> <title> <color>
    // rules category delete <id>
    // rules category set <id> [title] [color] [header] [footer]
    // rules category list
    // rules category update <id> <channel>

    // rules manager add <id> <rule>
    // rules manager remove <id> <index>
    // rules manager edit <id> <index> <new_rule>

    companion object {
        val rules = mutableListOf<ServerRulesCategory>()
        private val rulesFile = File(SprinklesCore.instance.config.storage.management, "rules.yml")

        @Serializable
        private data class ServerRulesStorage(
            val rules: List<ServerRulesCategory>
        )

        init {
            load()
        }

        fun load() {
            if (!rulesFile.exists())
                return

            rules.addAll(Yaml.default.decodeFromString(ServerRulesStorage.serializer(), rulesFile.readText()).rules)
        }

        fun save() {
            if (!rulesFile.exists())
                rulesFile.createNewFile()

            rulesFile.writeText(Yaml.default.encodeToString(ServerRulesStorage.serializer(), ServerRulesStorage(rules)))
        }
    }

    @SubCommandHolder("category", "Rules categories")
    class Category {
        @SlashSubCommand
        suspend fun create(
            ctx: SlashContext,

            id: String,
            title: String,
            color: String
        ) {
            if (rules.any { it.id == id }) {
                ctx.sendPrivate("ID $id already exists!")
                return
            }

            if (title.length > 256) {
                ctx.sendPrivate("Title is too long!")
                return
            }

            if (color.removePrefix("#").length != 6) {
                ctx.sendPrivate("Invalid color hex string!")
                return
            }

            val colorHex = color.removePrefix("#").toInt(16)

            val category = ServerRulesCategory(id, title, colorHex)

            rules.add(category)
            save()

            ctx.sendPrivateEmbed {
                this.setAuthor("(PREVIEW)")
                this.setTitle(title)
                this.setColor(colorHex)
                this.setFooter("ID: $id")
            }
        }

        @SlashSubCommand
        suspend fun delete(
            ctx: SlashContext,
            id: String
        ) {
            if (rules.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            rules.removeIf { it.id == id }
            save()

            ctx.sendPrivate("Successfully deleted rules category!")
        }

        @SlashSubCommand
        suspend fun set(
            ctx: SlashContext,
            id: String,
            title: String? = null,
            color: String? = null,
            header: String? = null,
            footer: String? = null
        ) {
            if (rules.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            val category = rules.first { it.id == id }

            if (!color.isNullOrBlank()) {
                if (color.removePrefix("#").length != 6) {
                    ctx.sendPrivate("Invalid color hex string!")
                    return
                }

                val colorHex = color.removePrefix("#").toInt(16)
                category.color = colorHex
            }

            if (!title.isNullOrBlank())
                category.name = title

            category.header = header ?: ""
            category.footer = footer ?: ""

            save()
            ctx.sendPrivate("Updated header and footer!")
        }

        @SlashSubCommand
        suspend fun list(
            ctx: SlashContext
        ) {
            if (rules.isEmpty()) {
                ctx.sendPrivate("There are no rules categories!")
                return
            }

            ctx.sendPrivate(rules.joinToString("\n") { "${it.name} ({${it.id})" })
        }

        @SlashSubCommand
        suspend fun update(
            ctx: SlashContext,
            id: String,
            channel: TextChannel
        ) {
            if (rules.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            val category = rules.first { it.id == id }

            if (category.channelToMessage.containsKey(channel.idLong)) {
                val messageId = category.channelToMessage[channel.idLong]!!

                channel.deleteMessageById(messageId).queue()
            }

            val message = MessageCreate {
                embed {
                    title = category.name
                    color = category.color

                    description = category.header
                    if (category.header.isNotBlank())
                        description += "\n\n"

                    for ((index, rule) in category.rules.withIndex()) {
                        description += "**${index + 1}.** $rule\n"
                    }

                    if (category.footer.isNotBlank())
                        description += "\n${category.footer}"

                    footer {
                        name = "ID: ${category.id}"
                    }
                }
            }

            channel.sendMessage(message).queue()
            ctx.sendPrivate("sent update")
        }
    }

    @SubCommandHolder("manager", "Rules management")
    class Manager {
        @SlashSubCommand
        suspend fun add(
            ctx: SlashContext,
            id: String, rule: String
        ) {
            if (rules.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            val category = rules.first { it.id == id }
            category.rules.add(rule)
            save()
            ctx.sendPrivate("Added rule ${category.rules.size} to ${category.name}")
        }

        @SlashSubCommand
        suspend fun remove(
            ctx: SlashContext,
            id: String,
            @Description("Index from 1, 2, 3...")
            index: Int
        ) {
            if (rules.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            val category = rules.first { it.id == id }

            val rule = category.rules.removeAt(index - 1)
            save()

            ctx.sendPrivate("Successfully removed rule $index: \"$rule\" from ${category.name}.")
        }

        @SlashSubCommand
        suspend fun edit(
            ctx: SlashContext,
            id: String,
            @Description("Index from 1, 2, 3...")
            index: Int,

            @Name("new_rule")
            newRule: String
        ) {
            if (rules.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            val category = rules.first { it.id == id }

            val oldRule = category.rules[index - 1]

            category.rules[index - 1] = newRule
            save()

            ctx.sendPrivate("Successfully changed rule $index: \"$oldRule\" -> \"$newRule\" from ${category.name}.")
        }
    }
}