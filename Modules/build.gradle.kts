plugins {
    kotlin("jvm")
}

subprojects {
    apply(plugin = "org.jetbrains.dokka")

    repositories { mavenCentral() }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test { useJUnitPlatform() }
}
