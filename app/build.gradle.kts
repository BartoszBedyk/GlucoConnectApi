plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.example.ApplicationKt")
    val isDevelopment = project.hasProperty("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val h2Version: String by project
val postgresVersion: String by project
val liquibaseVersion: String by project
val serializationVersion: String by project
val dotenvVersion: String by project
val gsonVersion: String by project
val koinVersion: String by project

dependencies {
    // Ktor core
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    // Serialization
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // DB & ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.h2database:h2:$h2Version")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.slf4j:slf4j-api:2.0.13")

    // Utils
    implementation("io.github.cdimascio:dotenv-kotlin:$dotenvVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")

    // PDF
    implementation("org.thymeleaf:thymeleaf:3.0.15.RELEASE")
    implementation("com.openhtmltopdf:openhtmltopdf-pdfbox:1.0.10")

    // Koin
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Modules
    implementation(project(":Common"))

    //Old modules
    implementation(project(":Modules:User-Module"))
    implementation(project(":Modules:Drug-Module"))
    implementation(project(":Modules:Observer-Module"))
    implementation(project(":Modules:HeartbeatResult-Module"))
    implementation(project(":Modules:ResearchResult-Module"))

    //New modules
    implementation(project(":Modules:Glucose-Module"))
    implementation(project(":Modules:Activity-Module"))




    // Test
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
