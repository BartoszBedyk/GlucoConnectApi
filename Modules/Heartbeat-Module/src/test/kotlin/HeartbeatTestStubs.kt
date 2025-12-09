import java.io.File

interface HeartbeatTestStubs {
    val jsonPost: String
        get() = File("src/test/resources/heartbeat_create_input.json").readText()

    val userId: String
        get() = "276aa762-9b5f-4d01-96d1-ab1d510d9c36"
}
