package com.example.glucoses

import java.io.File

interface GlucoseTestStubs {

    val jsonPost: String
        get() = File("src/test/resources/glucoses/glucose_create_input.json").readText()

    val glucoseId: String
        get() = "1b385a01-ab6b-43e3-a821-2dfaa5f3e385"
}
