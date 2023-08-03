package xyz.bluspring.sprinkles.discord.commands.roles

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import xyz.artrinix.aviation.annotations.Name
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.*
import xyz.artrinix.aviation.entities.Scaffold
import xyz.bluspring.sprinkles.discord.modules.roles.AssignableRole
import xyz.bluspring.sprinkles.discord.modules.roles.RoleCategory
import xyz.bluspring.sprinkles.discord.modules.roles.RoleManagerModule
import java.util.*

@SlashCommand("role", "Role commands", guildOnly = true, defaultUserPermissions = [ Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS ])
class RoleCommand : Scaffold {
    // role category create <id> <name> <description> <color>
    // role category delete <id>
    // role category list
    // role category update <id> <#channel>

    // role manager add <category_id> <role> <displayName> <description>
    // role manager remove <category_id> <role>
    // role manager list <category_id>

    @SubCommandHolder("manager", "Role management")
    class Manager {
        @SlashSubCommand("Adds roles to role category")
        suspend fun add(
            ctx: SlashContext,

            @Name("category_id")
            categoryId: String,

            role: Role,

            @Name("display_name")
            displayName: String,
            description: String
        ) {
            if (RoleManagerModule.roles.none { it.id == categoryId }) {
                ctx.sendPrivate("ID $categoryId does not exist!")
                return
            }

            val category = RoleManagerModule.roles.first { it.id == categoryId }

            if (category.roles.any { it.roleId == role.idLong }) {
                ctx.sendPrivate("Role ${role.asMention} already exists!")
                return
            }

            val assignable = AssignableRole(
                role.idLong,
                displayName, description
            )

            category.roles.add(assignable)
            RoleManagerModule.save()
            ctx.sendPrivate("Added role ${role.asMention} to category \"${category.name}\"!")
        }

        @SlashSubCommand("Removes role from role category")
        suspend fun remove(
            ctx: SlashContext,

            @Name("category_id")
            categoryId: String,
            role: Role
        ) {
            if (RoleManagerModule.roles.none { it.id == categoryId }) {
                ctx.sendPrivate("ID $categoryId does not exist!")
                return
            }

            val category = RoleManagerModule.roles.first { it.id == categoryId }

            if (category.roles.none { it.roleId == role.idLong }) {
                ctx.sendPrivate("Role ${role.asMention} does not exist in category!")
                return
            }

            category.roles.removeIf { it.roleId == role.idLong }

            RoleManagerModule.save()
            ctx.sendPrivate("Removed role ${role.asMention} from category \"${category.name}\"!")
        }

        @SlashSubCommand("Lists roles in role category")
        suspend fun list(
            ctx: SlashContext,

            @Name("category_id")
            categoryId: String
        ) {
            if (RoleManagerModule.roles.none { it.id == categoryId }) {
                ctx.sendPrivate("ID $categoryId does not exist!")
                return
            }

            val category = RoleManagerModule.roles.first { it.id == categoryId }

            ctx.sendPrivate(category.roles.joinToString("\n") {
                "${it.displayName} (<@&${it.roleId}>) - ${it.description}"
            })
        }
    }

    @SubCommandHolder("category", "Role category management")
    class Category {
        @SlashSubCommand("Creates a role category for messages to be directed to.")
        suspend fun create(
            ctx: SlashContext,

            @Description("The category ID, to make it easier to identify and edit.")
            id: String,

            @Description("The category name")
            name: String,
            description: String,

            color: String
        ) {
            if (RoleManagerModule.roles.any { it.id == id }) {
                ctx.sendPrivate("ID $id has already been used!")
                return
            }

            if (name.length > 256) {
                ctx.sendPrivate("Name is too long!")
                return
            }

            if (description.length > 4096) {
                ctx.sendPrivate("Description is too long!")
                return
            }

            if (color.removePrefix("#").length != 6) {
                ctx.sendPrivate("Invalid color hex string!")
                return
            }

            val colorHex = color.removePrefix("#").toInt(16)

            val category = RoleCategory(
                id, name, description,
                colorHex
            )

            RoleManagerModule.roles.add(category)
            RoleManagerModule.save()

            ctx.sendPrivateEmbed {
                this.setAuthor("$id (PREVIEW)")
                this.setTitle(name)
                this.setDescription(description)
                this.setColor(colorHex)
            }
        }

        @SlashSubCommand("Deletes the role category")
        suspend fun delete(
            ctx: SlashContext,
            id: String
        ) {
            if (RoleManagerModule.roles.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            RoleManagerModule.roles.removeIf { it.id == id }
            RoleManagerModule.save()

            ctx.sendPrivate("Successfully deleted role category $id!")
        }

        @SlashSubCommand("Lists all available role categories")
        suspend fun list(
            ctx: SlashContext
        ) {
            if (RoleManagerModule.roles.isEmpty()) {
                ctx.sendPrivate("No available role categories!")
                return
            }

            ctx.sendPrivate(RoleManagerModule.roles.joinToString("\n") {
                "${it.name} (ID: **${it.id}**): ${it.roles.size} roles"
            })
        }

        @SlashSubCommand("(Re-)Sends the role category message into the respective channel")
        suspend fun update(
            ctx: SlashContext,
            id: String,
            channel: TextChannel
        ) {
            if (RoleManagerModule.roles.none { it.id == id }) {
                ctx.sendPrivate("ID $id does not exist!")
                return
            }

            val category = RoleManagerModule.roles.first { it.id == id }

            if (category.roles.isEmpty()) {
                ctx.sendPrivate("No available assignable roles have been given!")
                return
            }

            val categoryId = "sprinkles:${category.id}:${UUID.randomUUID().toString().replace("-", "")}"

            val message = MessageCreate {
                embed {
                    author {
                        name = category.id
                    }
                    title = category.name
                    color = category.color
                    description = category.description
                }

                val selections = mutableListOf<SelectOption>()

                for (role in category.roles) {
                    selections.add(SelectOption(
                        role.displayName,
                        role.roleId.toString(),
                        role.description
                    ))
                }

                actionRow(StringSelectMenu(
                    categoryId,
                    "None selected",
                    1..selections.size,
                    false,
                    selections
                ))
            }

            if (category.channelToMessage.containsKey(channel.idLong)) {
                val messageId = category.channelToMessage[channel.idLong]!!.first

                channel.deleteMessageById(messageId).queue()
            }

            channel.sendMessage(message).queue {
                category.channelToMessage[channel.idLong] = Pair(it.idLong, categoryId)
                RoleManagerModule.save()
            }
            ctx.sendPrivate("Updated channel in <#${channel.id}> message!")
        }
    }
}