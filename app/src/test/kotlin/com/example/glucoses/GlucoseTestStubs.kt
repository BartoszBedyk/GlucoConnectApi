package com.example.glucoses

import java.io.File

interface GlucoseTestStubs {

    val jsonPost: String
        get() = File("src/test/resources/glucoses/glucose_create_input.json").readText()
}
