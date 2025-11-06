import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    id("io.ktor.plugin") version "2.3.12" apply false
    id("org.liquibase.gradle") version "2.0.4" apply false
    id("org.jetbrains.dokka") version "1.9.0" apply false
    id("com.diffplug.spotless") version "6.25.0" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.1" apply false
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    group = "com.example"
    version = "0.0.1"

    apply(plugin = "org.jetbrains.kotlin.jvm")

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

subprojects {
    plugins.apply("com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            ktlint("1.3.1").editorConfigOverride(
                mapOf(
                    "indent_size" to "4",
                    "max_line_length" to "120",
                    "insert_final_newline" to "true"
                )
            )
            trimTrailingWhitespace()
            endWithNewline()
        }

        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint()
        }

        format("misc") {
            target("*.md", "*.gitignore")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    plugins.apply("org.jetbrains.dokka")

    if (tasks.findByName("dokkaHtml") == null) {
        tasks.register<DokkaTask>("dokkaHtml") {
            outputDirectory.set(layout.buildDirectory.dir("dokka"))

            dokkaSourceSets {
                named("main") {
                    displayName.set(project.name)
                    // includes.from("ModuleDescription.md")
                }
            }
        }
    }


    plugins.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        config.from(rootProject.files("detekt-config.yml"))
        buildUponDefaultConfig = true
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget = "20"

        reports {
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.file("reports/detekt.html").get().asFile)

            xml.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("reports/detekt.xml").get().asFile)

            txt.required.set(false)
        }
    }

    tasks.register("detektAll") {
        group = "verification"
        description = "Run detekt on all source sets"
        dependsOn("detekt")
    }
}

tasks.register("dokkaHtmlAll") {
    group = "documentation"
    description = "Generates Dokka HTML for all modules"
    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("dokkaHtml")
        }
    )
}
