package xyz.bluspring.sprinkles.discord.modules.roles

import kotlinx.serialization.Serializable

@Serializable
data class AssignableRole(
    val roleId: Long,
    val displayName: String,
    val description: String
)
