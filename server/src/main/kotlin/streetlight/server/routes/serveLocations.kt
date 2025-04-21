package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import klutch.utils.getUserId
import streetlight.model.Api
import streetlight.server.db.services.LocationApiService

fun Routing.serveLocations(service: LocationApiService = LocationApiService()) {
    getById(Api.Locations.Area) { id, endpoint ->
        service.readLocations(id.toInt())
    }

    getById(Api.Locations) { id, endpoint ->
        service.readLocation(id.toInt())
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