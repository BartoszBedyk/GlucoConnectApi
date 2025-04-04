// Define project properties
val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project
val h2_version: String by project
val postgres_version: String by project

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id ("org.liquibase.gradle") version "2.0.4"
    id("org.jetbrains.dokka") version "2.0.0"
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

sourceSets {
    main {
        resources {
            srcDir("../app/src/main/resources")
            srcDir ("../Modules/ResearchResult-Module/src/main/resources"
            )
        }
    }
}

tasks.withType<org.gradle.api.tasks.Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}


group = "com.example"
version = "0.0.1"

application {
    mainClass.set("com.example.ApplicationKt")


    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {

    mavenCentral()
}



dependencies {
    // Ktor core dependencies
    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-host-common-jvm:2.3.12")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")

    // Ktor serialization with Kotlinx JSON
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Exposed ORM framework dependencies for database interaction
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")

    // Database drivers for H2 and PostgreSQL
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.h2database:h2:$h2_version")
    implementation("org.postgresql:postgresql:42.7.2")

    // Logging with Logback and SLF4J
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.slf4j:slf4j-api:2.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")



    // Testing dependencies
    testImplementation("io.ktor:ktor-server-test-host-jvm:2.3.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

