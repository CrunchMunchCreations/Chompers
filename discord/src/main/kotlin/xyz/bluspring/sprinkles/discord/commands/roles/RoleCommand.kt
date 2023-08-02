package xyz.bluspring.sprinkles.discord.commands.roles

import net.dv8tion.jda.api.Permission
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.*
import xyz.artrinix.aviation.entities.Scaffold

@SlashCommand("role", "Role commands", guildOnly = true, defaultUserPermissions = [ Permission.MANAGE_ROLES, Permission.MODERATE_MEMBERS ])
class RoleCommand : Scaffold {
    // role category create <id> <name> <description> <color>
    // role category delete <id>
    // role category list
    // role category update <#channel>

    // role add <category_id> <role_name> [color]
    // role remove <category_id> <role_name>
    // role list <category_id>

    @SubCommandHolder("category", "Creates a role category for messages to be directed to.")
    class Category {
        suspend fun create(
            ctx: SlashContext,

            @Description("The category ID, to make it easier to identify and edit.")
            id: String,

            @Description("The category name")
            name: String,
            description: String,

            color: String
        ) {

        }
    }
}