repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))

    api("net.dv8tion:JDA:5.0.0-beta.24") {
        exclude(module = "opus-java")
    }
    api("xyz.artrinix:aviation:8ff17412")
    api("club.minnced:jda-ktx:0.11.0-beta.20")
    api(kotlin("scripting-jsr223"))
}