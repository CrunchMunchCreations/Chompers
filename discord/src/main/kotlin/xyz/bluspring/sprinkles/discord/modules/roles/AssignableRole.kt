package xyz.bluspring.sprinkles.discord.modules.roles

import kotlinx.serialization.Serializable

@Serializable
data class AssignableRole(
    val id: String,
    val roleId: Long,
    val color: Int
)
