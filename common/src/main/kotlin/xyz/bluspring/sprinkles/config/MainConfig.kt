package xyz.bluspring.sprinkles.config

import kotlinx.serialization.Serializable

@Serializable
data class MainConfig(
    val api: ApiConfigs = ApiConfigs()
)
