package xyz.bluspring.sprinkles.discord.modules.roles.storage

import kotlinx.serialization.Serializable

@Serializable
data class AssignableRole(
    val roleId: Long,
    var displayName: String,
    var description: String
)
