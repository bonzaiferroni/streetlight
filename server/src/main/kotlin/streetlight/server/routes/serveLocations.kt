package streetlight.server.routes

import io.ktor.server.routing.Routing
import klutch.server.*
import streetlight.model.Api
import streetlight.server.db.services.LocationDtoService

fun Routing.serveLocations(service: LocationDtoService = LocationDtoService()) {
    getById(Api.Locations) { id, endpoint ->
        service.readLocations(id.toInt())
    }

    authenticateJwt {
        post(Api.Locations.Create) { newLocation, endpoint ->
            service.createLocation(newLocation)
        }
    }
}