rootProject.name = "ktor-backend"

include(
    ":app",
    ":Common",
    ":Modules",
    ":Modules:Drug-Module",
    ":Modules:Observer-Module",
    ":Modules:ResearchResult-Module",
    ":Modules:HeartbeatResult-Module",
    ":Modules:Glucose-Module",
    ":Modules:Activity-Module",
    ":Modules:User-Module",
    ":Modules:Heartbeat-Module",
    ":Modules:Authentication-Module"
)
