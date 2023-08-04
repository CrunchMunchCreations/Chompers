package xyz.bluspring.sprinkles.discord.modules.roles

import com.charleskorn.kaml.Yaml
import dev.minn.jda.ktx.events.listener
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberUpdateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import xyz.artrinix.aviation.entities.AbstractModule
import xyz.bluspring.sprinkles.SprinklesCore
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.discord.modules.roles.storage.StickyRolesStorage
import java.io.File

class StickyRolesModule : AbstractModule {
    val memberToRoles = mutableMapOf<Long, MutableSet<Long>>()
    private val stickyRolesFile = File(SprinklesCore.instance.config.storage.management, "sticky_roles.yml")

    fun load() {
        if (!stickyRolesFile.exists())
            return

        val storage = Yaml.default.decodeFromString(StickyRolesStorage.serializer(), stickyRolesFile.readText())

        for ((member, roles) in storage.stickyRoles) {
            memberToRoles[member] = roles
        }
    }

    fun save() {
        if (!stickyRolesFile.exists())
            stickyRolesFile.createNewFile()

        stickyRolesFile.writeText(
            Yaml.default.encodeToString(
                StickyRolesStorage.serializer(), StickyRolesStorage(
                    memberToRoles
                )
            ))
    }

    override suspend fun onEnable() {
        load()

        val jda = SprinklesDiscord.instance.jda

        jda.listener<GuildMemberJoinEvent> { event ->
            if (memberToRoles.contains(event.member.idLong)) {
                val rolesList = memberToRoles[event.member.idLong]!!
                val loadedRoles = rolesList.mapNotNull { jda.getRoleById(it) }

                for (role in loadedRoles) {
                    try {
                        event.guild.addRoleToMember(event.member, role).queue()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        jda.listener<ReadyEvent> { _ ->
            val guild = jda.guilds.first()
            guild.loadMembers { member ->
                if (!memberToRoles.contains(member.idLong)) {
                    memberToRoles[member.idLong] = mutableSetOf()
                }

                if (memberToRoles[member.idLong]!!.size == member.roles.size)
                    return@loadMembers

                memberToRoles[member.idLong]!!.addAll(member.roles.map { it.idLong })
            }

            save()
        }

        jda.listener<GuildMemberUpdateEvent> { event ->
            val member = event.member

            if (!memberToRoles.contains(member.idLong)) {
                memberToRoles[member.idLong] = mutableSetOf()
            }

            if (memberToRoles[member.idLong]!!.size == member.roles.size)
                return@listener

            memberToRoles[member.idLong]!!.addAll(member.roles.map { it.idLong })
            save()
        }
    }

    override suspend fun onDisable() {
    }
}