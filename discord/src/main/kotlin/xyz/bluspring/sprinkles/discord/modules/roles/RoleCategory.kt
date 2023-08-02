package xyz.bluspring.sprinkles.discord.modules.roles

import kotlinx.serialization.Serializable

@Serializable
data class RoleCategory(
    val id: String,
    val name: String,
    val description: String,
    val color: Int,
    var channel: Long,

    val roles: List<AssignableRole>
)
