package xyz.bluspring.sprinkles.discord.commands.roles

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Role
import xyz.artrinix.aviation.command.slash.SlashContext
import xyz.artrinix.aviation.command.slash.annotations.SlashCommand
import xyz.artrinix.aviation.command.slash.annotations.SlashSubCommand
import xyz.artrinix.aviation.entities.Scaffold
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.discord.modules.roles.AutoRoleModule

@SlashCommand("autorole", defaultUserPermissions = [ Permission.MODERATE_MEMBERS, Permission.MANAGE_ROLES ])
class AutoRoleCommand : Scaffold {
    @SlashSubCommand("Add an automatic role to assign to members.")
    suspend fun add(
        ctx: SlashContext,
        role: Role
    ) {
        if (AutoRoleModule.autoRoles.contains(role.idLong)) {
            ctx.sendPrivate("Role already exists in autoroles!")
            return
        }

        AutoRoleModule.autoRoles.add(role.idLong)
        AutoRoleModule.save()
        ctx.sendPrivate("Added role ${role.asMention} to automatic roles!")
    }

    @SlashSubCommand("Remove automatic role")
    suspend fun remove(
        ctx: SlashContext,
        role: Role
    ) {
        if (!AutoRoleModule.autoRoles.contains(role.idLong)) {
            ctx.sendPrivate("Role doesn't exist in autoroles!")
            return
        }

        AutoRoleModule.autoRoles.remove(role.idLong)
        AutoRoleModule.save()

        ctx.sendPrivate("Removed role ${role.asMention} from automatic roles!")
    }

    @SlashSubCommand
    suspend fun list(
        ctx: SlashContext
    ) {
        val autoRoles = AutoRoleModule.autoRoles.mapNotNull { SprinklesDiscord.instance.jda.getRoleById(it) }
        val deadRoles = AutoRoleModule.autoRoles.filter { autoRoles.none { a -> a.idLong == it } }

        if (AutoRoleModule.autoRoles.removeAll(deadRoles)) {
            ctx.sendPrivate("(automatically purged dead roles: ${deadRoles.joinToString(", ")})")
            AutoRoleModule.save()
        }

        ctx.sendPrivate("Auto-roles: ${autoRoles.joinToString(", ") { it.asMention }} (${autoRoles.size})")
    }
}