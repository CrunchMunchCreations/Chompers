package xyz.bluspring.sprinkles.twitch.commands.custom.management

import kotlinx.serialization.Serializable

@Serializable
data class CustomCommandDefinition(
    val name: String,
    val args: MutableMap<String, BrigadierArgument> = mutableMapOf(),
    val optionalArgs: MutableList<String> = mutableListOf(),
    val custom: MutableList<CustomArgument> = mutableListOf(),
    var response: String,
    var enabled: Boolean = true,
    var reply: Boolean = true,
    var permissionLevel: PermissionLevel = PermissionLevel.USER,

    var globalCooldown: Long = 5000,
    var userCooldown: Long = 15000
)