package xyz.bluspring.sprinkles.discord.modules.roles.storage

import kotlinx.serialization.Serializable

@Serializable
data class RoleCategory(
    val id: String,
    var name: String,
    var description: String,
    var color: Int,

    var removeRoleName: String? = null,
    var removeRoleDesc: String? = null,

    // text channel ID -> message ID, selection ID
    val channelToMessage: MutableMap<Long, Pair<Long, String>> = mutableMapOf(),
    val roles: MutableList<AssignableRole> = mutableListOf(),
)
