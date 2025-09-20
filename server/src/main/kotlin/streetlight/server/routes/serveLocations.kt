package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.model.data.toProjectId
import streetlight.server.db.services.LocationApiService

fun Routing.serveLocations(service: LocationApiService = LocationApiService()) {
    get(Api.Locations.Area, { it.toProjectId() }) { id, endpoint ->
        service.readLocations(id)
    }

    get(Api.Locations, { it.toProjectId() }) { id, endpoint ->
        service.readLocation(id)
    }

    authenticateJwt {
        post(Api.Locations.Create) { newLocation, endpoint ->
            service.createLocation(newLocation)
        }

        post(Api.Locations.Update) { location, endpoint ->
            val userId = call.getUserId()
            if (userId != location.userId) {
                throw UnauthorizedUserException()
            }
            service.updateLocation(userId, location)
        }
    }
}