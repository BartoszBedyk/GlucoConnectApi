plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories { mavenCentral() }

val ktorVersion: String by project
val liquibaseVersion: String by project
val serializationVersion: String by project
val jwtVersion: String by project

dependencies {
    implementation(project(":Common"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("com.auth0:java-jwt:$jwtVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")

    testImplementation(kotlin("test"))
}
