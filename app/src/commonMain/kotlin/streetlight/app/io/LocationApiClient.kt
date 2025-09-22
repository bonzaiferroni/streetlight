package streetlight.app.io

import kabinet.api.write
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.AreaId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewLocation

class LocationApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readLocation(locationId: LocationId) = client.getById(Api.Locations, locationId)
    suspend fun readAreaLocations(areaId: AreaId) = client.getById(Api.Locations.Area, areaId)
    suspend fun createLocation(newLocation: NewLocation) = client.request(Api.Locations.Create, newLocation)
    suspend fun updateLocation(location: Location) = client.request(Api.Locations.Update, location)
    suspend fun search(query: String) = client.request(Api.Locations.Search) {
        write(it.query, query)
    }
}