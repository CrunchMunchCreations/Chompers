package xyz.bluspring.sprinkles.discord.modules.roles

import com.charleskorn.kaml.Yaml
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import xyz.artrinix.aviation.entities.AbstractModule
import xyz.bluspring.sprinkles.SprinklesCore
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import java.io.File

class AutoRoleModule : AbstractModule {
    override suspend fun onEnable() {
        load()

        val jda = SprinklesDiscord.instance.jda

        jda.listener<GuildMemberJoinEvent> { event ->
            for (roleId in autoRoles) {
                event.guild.addRoleToMember(event.member, event.guild.getRoleById(roleId) ?: continue).queue()
            }
        }
    }

    override suspend fun onDisable() {
    }

    companion object {
        val autoRoles = mutableListOf<Long>()
        private val autoRoleFile = File(SprinklesCore.instance.config.storage.management, "auto_roles.yml")

        fun load() {
            if (!autoRoleFile.exists())
                return

            autoRoles.addAll(Yaml.default.decodeFromString(AutoRoleStorage.serializer(), autoRoleFile.readText()).roleIds)
        }

        fun save() {
            if (!autoRoleFile.exists())
                autoRoleFile.createNewFile()

            autoRoleFile.writeText(Yaml.default.encodeToString(AutoRoleStorage.serializer(), AutoRoleStorage(autoRoles)))
        }
    }
}