rootProject.name = "ktor-backend"

include(
    ":app",
    ":Common",
    ":Modules",
    ":Modules:User-Module",
    ":Modules:Drug-Module",
    ":Modules:Observer-Module",
    ":Modules:ResearchResult-Module",
    ":Modules:HeartbeatResult-Module",
    ":Modules:Glucose-Module",
    ":Modules:Activity-Module",
    ":Modules:User2-Module"
)
include("Modules:User-Module2")
findProject(":Modules:User-Module2")?.name = "User-Module2"
