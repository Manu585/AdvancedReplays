plugins {
    id("java-library")
}

dependencies {
    api(project(":advancedreplays-api"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("org.mariadb.jdbc:mariadb-java-client:3.4.1")
}
