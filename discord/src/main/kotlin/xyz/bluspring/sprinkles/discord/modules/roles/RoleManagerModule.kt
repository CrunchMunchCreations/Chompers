package xyz.bluspring.sprinkles.discord.modules.roles

import com.charleskorn.kaml.Yaml
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.listener
import kotlinx.serialization.Serializable
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import xyz.artrinix.aviation.entities.AbstractModule
import xyz.bluspring.sprinkles.SprinklesCore
import xyz.bluspring.sprinkles.discord.SprinklesDiscord
import xyz.bluspring.sprinkles.discord.modules.roles.storage.RoleCategory
import java.io.File

class RoleManagerModule : AbstractModule {
    override suspend fun onEnable() {
        load()

        val jda = SprinklesDiscord.instance.jda

        jda.listener<StringSelectInteractionEvent> { event ->
            val selectionId = event.interaction.selectMenu.id

            val category = roles.firstOrNull { it.channelToMessage[event.channel.idLong]?.second == selectionId }
                ?: return@listener

            val msg = event.deferReply(true).await()

            val allRoles = category.roles.mapNotNull { event.guild!!.getRoleById(it.roleId) }
            val selectedRoles = event.selectedOptions.mapNotNull { event.guild!!.getRoleById(it.value) }

            event.guild!!.modifyMemberRoles(event.member!!, selectedRoles, allRoles.filter { !selectedRoles.contains(it) }).await()
            msg.editOriginal("Your roles have been edited successfully!").await()
        }
    }

    override suspend fun onDisable() {
    }

    companion object {
        val roles = mutableListOf<RoleCategory>()
        private val rolesFile = File(SprinklesCore.instance.config.storage.management, "roles.yml")

        @Serializable
        private data class RoleFileStorage(
            val roles: List<RoleCategory>
        )

        fun save() {
            if (!rolesFile.exists())
                rolesFile.createNewFile()

            rolesFile.writeText(Yaml.default.encodeToString(RoleFileStorage.serializer(), RoleFileStorage(roles)))
        }

        fun load() {
            if (!rolesFile.exists())
                return

            val data = SprinklesCore.yaml.decodeFromString(RoleFileStorage.serializer(), rolesFile.readText())
            roles.addAll(data.roles)
        }
    }
}