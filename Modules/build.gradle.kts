plugins {
    kotlin("jvm")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")
}


group = "com.example"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}