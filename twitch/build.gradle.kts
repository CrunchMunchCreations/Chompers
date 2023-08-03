
dependencies {
    implementation(project(":common"))

    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    api("org.kitteh.irc:client-lib:8.0.0")

    // i thought it would be incredibly funny if a Twitch chatbot
    // used a Minecraft command dispatcher
    api("com.mojang:brigadier:1.1.8")
}