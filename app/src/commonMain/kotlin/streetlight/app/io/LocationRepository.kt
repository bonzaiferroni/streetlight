package streetlight.app.io

import kabinet.api.write
import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.StreetId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewLocation
import streetlight.model.mockDb

interface LocationRepository {
    suspend fun readLocation(locationId: LocationId): Location?
    suspend fun readAreaLocations(streetId: StreetId): List<Location>?
    suspend fun createLocation(newLocation: NewLocation): LocationId?
    suspend fun updateLocation(location: Location): Boolean?
    suspend fun search(query: String): List<Location>?
    suspend fun readTop(count: Int = 10): List<Location>?
}

class LocationApiClient(
    private val client: NeoApiClient
): LocationRepository {
    override suspend fun readLocation(locationId: LocationId) = client.getById(Api.LocationFeed, locationId)
    override suspend fun readAreaLocations(streetId: StreetId) = client.getById(Api.LocationFeed.Street, streetId)
    override suspend fun createLocation(newLocation: NewLocation) = client.request(Api.LocationFeed.Create, newLocation)
    override suspend fun updateLocation(location: Location) = client.request(Api.LocationFeed.Update, location)
    override suspend fun search(query: String) = client.request(Api.LocationFeed.Search) {
        write(it.query, query)
    }
    override suspend fun readTop(count: Int) = client.request(Api.LocationFeed.ReadTop) {
        write(it.count, count)
    }
}

class LocationMockClient: LocationRepository {
    override suspend fun readLocation(locationId: LocationId): Location? = mockDb.locations.firstOrNull( { it.locationId == locationId })
    override suspend fun readAreaLocations(streetId: StreetId): List<Location>? = mockDb.locations.filter { it.streetId == streetId }
    override suspend fun createLocation(newLocation: NewLocation): LocationId? = TODO("Not yet implemented")
    override suspend fun updateLocation(location: Location): Boolean? = TODO("Not yet implemented")
    override suspend fun search(query: String): List<Location>? = mockDb.locations.filter { it.name.contains(query, ignoreCase = true) }
    override suspend fun readTop(count: Int): List<Location>? = mockDb.locations.take(count)
}
