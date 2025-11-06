rootProject.name = "ktor-backend"

include(
    ":app",
    ":Common",
    ":Modules",
    ":Modules:User-Module",
    ":Modules:Drug-Module",
    ":Modules:Observer-Module",
    ":Modules:Activity-Module",
    ":Modules:ResearchResult-Module",
    ":Modules:HeartbeatResult-Module"
)
