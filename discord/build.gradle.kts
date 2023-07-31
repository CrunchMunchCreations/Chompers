dependencies {
    implementation(project(":common"))

    implementation("net.dv8tion:JDA:5.0.0-beta.12") {
        exclude(module = "opus-java")
    }
    implementation("xyz.artrinix:aviation:f5aa7faf")
    implementation("com.github.MinnDevelopment:jda-ktx:9370cb13cc")
}