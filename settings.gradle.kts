rootProject.name = "ktor-backend"


include("app")
include("Modules")
include("Modules:ResearchResult-Module")
findProject(":Modules:ResearchResult-Module")?.name = "ResearchResult-Module"
include("Common")
include("Modules:User-Module")
findProject(":Modules:User-Module")?.name = "User-Module"
include("Modules:Activity-Module")
findProject(":Modules:Activity-Module")?.name = "Activity-Module"
include("Modules:HeartbeatResult-Module")
findProject(":Modules:HeartbeatResult-Module")?.name = "HeartbeatResult-Module"
include("Modules:Drug-Module")
findProject(":Modules:Drug-Module")?.name = "Drug-Module"
include("Modules:Observer-Module")
findProject(":Modules:Observer-Module")?.name = "Observer-Module"
