package xyz.bluspring.sprinkles.discord.modules.roles.storage

import kotlinx.serialization.Serializable

@Serializable
data class StickyRolesStorage(
    val stickyRoles: Map<Long, MutableSet<Long>>
)
