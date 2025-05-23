plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.0.20"
}

group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}
sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-host-common-jvm:2.3.12")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")

    implementation("io.ktor:ktor-server-auth-jvm:2.3.12")
    implementation("io.ktor:ktor-server-auth:2.3.12")
    implementation("com.auth0:java-jwt:3.18.1")

    implementation(project(":Common"))
    implementation("org.liquibase:liquibase-core:4.23.0")
}

tasks.test {
    useJUnitPlatform()
}