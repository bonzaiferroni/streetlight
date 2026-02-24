package streetlight.app.io

import kampfire.api.write
import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.Place
import streetlight.model.mockDb

interface LocationRepository {
    suspend fun readLocation(locationId: LocationId): Location?
    suspend fun createLocation(place: Place): LocationId?
    suspend fun updateLocation(location: Location): Boolean?
    suspend fun search(query: String): List<Location>?
    suspend fun readTop(count: Int = 10): List<Location>?
}

class LocationApiClient(
    private val client: NeoApiClient
): LocationRepository {
    override suspend fun readLocation(locationId: LocationId) = client.getById(Api.Locations, locationId)
    override suspend fun createLocation(place: Place) = client.request(Api.Locations.Create, place)
    override suspend fun updateLocation(location: Location) = client.request(Api.Locations.Update, location)
    override suspend fun search(query: String) = client.request(Api.Locations.Search) {
        write(it.query, query)
    }
    override suspend fun readTop(count: Int) = client.request(Api.Locations.ReadTop) {
        write(it.count, count)
    }
}

class LocationMockClient: LocationRepository {
    override suspend fun readLocation(locationId: LocationId): Location? = mockDb.locations.firstOrNull( { it.locationId == locationId })
    override suspend fun createLocation(place: Place): LocationId? = TODO("Not yet implemented")
    override suspend fun updateLocation(location: Location): Boolean? = TODO("Not yet implemented")
    override suspend fun search(query: String): List<Location>? = mockDb.locations.filter { it.name.contains(query, ignoreCase = true) }
    override suspend fun readTop(count: Int): List<Location>? = mockDb.locations.take(count)
}
