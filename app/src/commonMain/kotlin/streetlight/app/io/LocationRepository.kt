package streetlight.app.io

import kabinet.api.write
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.AreaId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewLocation
import streetlight.model.mockDb

interface LocationRepository {
    suspend fun readLocation(locationId: LocationId): Location?
    suspend fun readAreaLocations(areaId: AreaId): List<Location>?
    suspend fun createLocation(newLocation: NewLocation): LocationId?
    suspend fun updateLocation(location: Location): Boolean?
    suspend fun search(query: String): List<Location>?
}

class LocationApiClient(
    private val client: NeoApiClient = globalNeoApiClient
): LocationRepository {
    override suspend fun readLocation(locationId: LocationId) = client.getById(Api.LocationFeed, locationId)
    override suspend fun readAreaLocations(areaId: AreaId) = client.getById(Api.LocationFeed.Area, areaId)
    override suspend fun createLocation(newLocation: NewLocation) = client.request(Api.LocationFeed.Create, newLocation)
    override suspend fun updateLocation(location: Location) = client.request(Api.LocationFeed.Update, location)
    override suspend fun search(query: String) = client.request(Api.LocationFeed.Search) {
        write(it.query, query)
    }
}

class LocationMockClient: LocationRepository {
    override suspend fun readLocation(locationId: LocationId): Location? = mockDb.locations.firstOrNull( { it.locationId == locationId })
    override suspend fun readAreaLocations(areaId: AreaId): List<Location>? = mockDb.locations.filter { it.areaId == areaId }
    override suspend fun createLocation(newLocation: NewLocation): LocationId? = TODO("Not yet implemented")
    override suspend fun updateLocation(location: Location): Boolean? = TODO("Not yet implemented")
    override suspend fun search(query: String): List<Location>? = mockDb.locations.filter { it.name.contains(query, ignoreCase = true) }
}
