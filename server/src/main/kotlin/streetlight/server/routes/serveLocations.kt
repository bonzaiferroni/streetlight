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
    getEndpoint(Api.LocationFeed.Area, { it.toProjectId() }) { id, _ ->
        dao.readLocations(id)
    }

    getEndpoint(Api.LocationFeed, { it.toProjectId() }) { id, _ ->
        dao.readLocation(id)
    }

    getEndpoint(Api.LocationFeed.Search) { endpoint ->
        val query = readParam(endpoint.query)
        dao.searchLocations(query)
    }

    authenticateJwt {
        postEndpoint(Api.LocationFeed.Create) { newLocation, _ ->
            val userId = getUserId()
            dao.createLocation(userId, newLocation)
        }

        postEndpoint(Api.LocationFeed.Update) { location, _ ->
            val userId = getUserId()
            if (userId != location.userId) {
                throw UnauthorizedUserException()
            }
            dao.updateLocation(userId, location)
        }
    }
}