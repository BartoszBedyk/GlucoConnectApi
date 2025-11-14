plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

subprojects {

    repositories { mavenCentral() }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test { useJUnitPlatform() }
}
