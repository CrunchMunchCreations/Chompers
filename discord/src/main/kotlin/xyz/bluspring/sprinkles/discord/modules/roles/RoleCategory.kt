package xyz.bluspring.sprinkles.discord.modules.roles

import kotlinx.serialization.Serializable

@Serializable
data class RoleCategory(
    val id: String,
    val name: String,
    val description: String,
    val color: Int,

    // text channel ID -> message ID, selection ID
    val channelToMessage: MutableMap<Long, Pair<Long, String>> = mutableMapOf(),
    val roles: MutableList<AssignableRole> = mutableListOf(),
)
