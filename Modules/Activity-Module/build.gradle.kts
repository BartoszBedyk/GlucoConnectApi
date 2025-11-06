
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
val jwtVersion: String by project

dependencies {
    implementation(project(":Common"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
