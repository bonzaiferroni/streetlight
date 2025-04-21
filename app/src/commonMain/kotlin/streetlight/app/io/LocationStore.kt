package streetlight.app.io

import pondui.io.ApiStore
import streetlight.model.Api
import streetlight.model.data.Location
import streetlight.model.data.NewLocation

class LocationStore: ApiStore() {
    suspend fun readLocation(locationId: Int) = client.get(Api.Locations, locationId)
    suspend fun readAreaLocations(areaId: Int) = client.get(Api.Locations.Area, areaId)
    suspend fun createLocation(newLocation: NewLocation) = client.post(Api.Locations.Create, newLocation)
    suspend fun updateLocation(location: Location) = client.post(Api.Locations.Update, location)
}