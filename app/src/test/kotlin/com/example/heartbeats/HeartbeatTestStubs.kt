package heartbeats

import java.io.File

interface HeartbeatTestStubs {
    val jsonPost: String
        get() = File("src/test/resources/heartbeats/heartbeat_create_input.json").readText()

    val heartbeatId: String
        get() = "224e700f-17c1-475e-a663-45edd91cc184"
}
