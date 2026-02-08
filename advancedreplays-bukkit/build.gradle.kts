plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(project(":advancedreplays-api"))
    implementation(project(":advancedreplays-core"))
    implementation(project(":advancedreplays-nms-v1_21_R0"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks {
    // normal jar off (avoid confusion)
    jar {
        enabled = false
    }

    // final plugin jar
    shadowJar {
        archiveClassifier.set("") // produces: advancedreplays-bukkit-<version>.jar
        // minimize() // optional; enable only if you've tested reflection/service loading paths
    }

    build {
        dependsOn(shadowJar)
    }
}
