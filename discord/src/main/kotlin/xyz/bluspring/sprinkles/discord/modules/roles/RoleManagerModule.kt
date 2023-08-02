package xyz.bluspring.sprinkles.discord.modules.roles

import xyz.artrinix.aviation.entities.AbstractModule

class RoleManagerModule : AbstractModule {
    override suspend fun onEnable() {

    }

    override suspend fun onDisable() {
    }

    companion object {
        val roles = mutableListOf<RoleCategory>()
    }
}