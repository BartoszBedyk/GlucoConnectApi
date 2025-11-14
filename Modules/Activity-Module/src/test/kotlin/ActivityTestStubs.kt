import java.io.File

interface ActivityTestStubs {
    val jsonPost: String
        get() = File("src/test/resources/activity_create_input.json").readText()

    val jsonRunning: String
        get() = File("src/test/resources/activity_create_running.json").readText()

    val jsonStopping: String
        get() = File("src/test/resources/activity_create_stopping.json").readText()
}
