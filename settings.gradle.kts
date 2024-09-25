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
