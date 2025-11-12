import java.io.File

open class ActivityTestStubs {
    val jsonPost = File("src/test/resources/activity_create_input.json").readText()
    val jsonRunning = File("src/test/resources/activity_create_running.json").readText()
    val jsonStopping = File("src/test/resources/activity_create_stopping.json").readText()
}
