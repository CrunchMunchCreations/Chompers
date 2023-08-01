plugins {
    kotlin("jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22" apply false
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

group = "xyz.bluspring"
version = property("version")!!

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        maven("https://gitlab.com/api/v4/projects/26794598/packages/maven")
        maven("https://jitpack.io")
        maven("https://m2.dv8tion.net/releases")
        maven("https://libraries.minecraft.net")
    }


    fun DependencyHandlerScope.include(
        dependencyNotation: String,
        action: Action<ExternalModuleDependency> = Action<ExternalModuleDependency> { }
    ) {
        shadow(dependencyNotation, action)
        implementation(dependencyNotation, action)
    }

    dependencies {
        include("org.jetbrains.kotlin:kotlin-stdlib")
        include("org.jetbrains.kotlin:kotlin-reflect")
        include("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-RC")

        include("org.slf4j:slf4j-api:2.0.5")
        include("ch.qos.logback:logback-classic:1.4.7")
        include("ch.qos.logback:logback-core:1.4.7")

        include("com.charleskorn.kaml:kaml:0.54.0")
    }

    kotlin {
        jvmToolchain(17)
    }

    tasks.shadowJar {
        archiveBaseName.set("Sprinkles")
        this.archiveVersion.set(property("version").toString())
    }
}