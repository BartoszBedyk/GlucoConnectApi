plugins {
    kotlin("jvm") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    id("io.ktor.plugin") version "2.3.12" apply false
    id("org.liquibase.gradle") version "2.0.4" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    group = "com.example"
    version = "0.0.1"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
