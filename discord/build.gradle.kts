dependencies {
    implementation(project(":common"))

    api("net.dv8tion:JDA:5.0.0-beta.12") {
        exclude(module = "opus-java")
    }
    api("xyz.artrinix:aviation:f5aa7faf")
    api("com.github.MinnDevelopment:jda-ktx:9370cb13cc")
}