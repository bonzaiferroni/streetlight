package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.model.data.toProjectId
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

fun Routing.serveLocations(app: ServerProvider = RuntimeProvider) {
    val dao = app.dao.location
    get(Api.Locations.Area, { it.toProjectId() }) { id, _ ->
        dao.readLocations(id)
    }

    get(Api.Locations, { it.toProjectId() }) { id, _ ->
        dao.readLocation(id)
    }

    get(Api.Locations.Search) { endpoint ->
        val query = readParam(endpoint.query)
        dao.searchLocations(query)
    }

    authenticateJwt {
        post(Api.Locations.Create) { newLocation, _ ->
            val userId = call.getUserId()
            dao.createLocation(userId, newLocation)
        }

        post(Api.Locations.Update) { location, _ ->
            val userId = call.getUserId()
            if (userId != location.userId) {
                throw UnauthorizedUserException()
            }
            dao.updateLocation(userId, location)
        }
    }
}