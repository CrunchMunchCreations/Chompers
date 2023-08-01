package xyz.bluspring.sprinkles.config

import kotlinx.serialization.Serializable

@Serializable
data class StorageConfigs(
    val notifications: String = "./",
    val auth: String = "./"
)
