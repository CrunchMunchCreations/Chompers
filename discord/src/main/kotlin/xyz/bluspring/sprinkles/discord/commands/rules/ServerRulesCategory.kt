package xyz.bluspring.sprinkles.discord.commands.rules

import kotlinx.serialization.Serializable

@Serializable
data class ServerRulesCategory(
    val id: String,
    var name: String,
    var color: Int,

    val channelToMessage: MutableMap<Long, Long> = mutableMapOf(),
    val rules: MutableList<String> = mutableListOf(),

    var header: String = "",
    var footer: String = ""
)
