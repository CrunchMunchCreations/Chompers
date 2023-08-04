package xyz.bluspring.sprinkles.discord.modules.roles.storage

import kotlinx.serialization.Serializable

@Serializable
data class AutoRoleStorage(
    val roleIds: MutableList<Long>
)
