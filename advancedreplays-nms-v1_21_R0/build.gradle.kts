plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

dependencies {
    api(project(":advancedreplays-api"))
    implementation(project(":advancedreplays-core"))

    // NMS access
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    // Usually still helpful for API types
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

// Build reobfuscated artifact for production
tasks.assemble {
    dependsOn(tasks.reobfJar)
}
