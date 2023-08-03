fun DependencyHandlerScope.include(
    dependencyNotation: String,
    action: Action<ExternalModuleDependency> = Action<ExternalModuleDependency> { }
) {
    shadow(dependencyNotation, action)
    implementation(dependencyNotation, action)
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shadowImpl(project(":common"))
    shadowImpl(project(":discord"))
    shadowImpl(project(":twitch"))
}


tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "xyz.bluspring.sprinkles.Sprinkles"
    }
}

tasks.shadowJar {
    configurations = listOf(shadowImpl)
}