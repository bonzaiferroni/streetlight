package streetlight.server.plugins

import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.Routing
import klutch.server.authenticateJwt
import klutch.server.postEndpoint
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider
import java.io.File

fun Routing.serveUserFiles(app: ServerProvider = RuntimeProvider) {
    authenticateJwt {
        postEndpoint(Api.Users.Images) { request, _ ->
            val userId = getUserId()
            app.dao.userFile.readUserFiles(userId, request.fileUse, request.count).map { it.url }
        }
    }
}

