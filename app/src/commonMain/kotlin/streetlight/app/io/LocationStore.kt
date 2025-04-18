package streetlight.app.io

import pondui.io.ApiStore
import streetlight.model.Api
import streetlight.model.data.NewLocation

class LocationStore: ApiStore() {
    suspend fun readLocations(areaId: Int) = client.get(Api.Locations, areaId)
    suspend fun createLocation(newLocation: NewLocation) = client.post(Api.Locations.Create, newLocation)
}