rootProject.name = "WebKt"
include("core")

// Include HTTP projects.
include("http")
include("http:common")
findProject(":http:common")?.name = "common"
include("http:client")
findProject(":http:client")?.name = "client"
include("http:server")
findProject(":http:server")?.name = "server"

// Include Websocket projects.
include("websocket")
include("websocket:common")
findProject(":websocket:common")?.name = "common"
include("websocket:client")
findProject(":websocket:client")?.name = "client"
include("websocket:server")
findProject(":websocket:server")?.name = "server"
