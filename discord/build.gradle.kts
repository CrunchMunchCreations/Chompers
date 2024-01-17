dependencies {
    implementation(project(":common"))

    api("net.dv8tion:JDA:5.0.0-beta.19") {
        exclude(module = "opus-java")
    }
    api("xyz.artrinix:aviation:1c462196")
    api("club.minnced:jda-ktx:0.11.0-beta.19")
}