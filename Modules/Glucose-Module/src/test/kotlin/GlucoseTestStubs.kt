import java.io.File

interface GlucoseTestStubs{

    val jsonPost: String
        get() = File("src/test/resources/glucose_create_input.json").readText()
}
