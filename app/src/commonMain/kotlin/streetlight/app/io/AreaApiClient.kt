package streetlight.app.io

import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewArea

class AreaApiClient(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readAll() = client.request(Api.AreaFeed)
    suspend fun createArea(newArea: NewArea) = client.request(Api.AreaFeed.Create, newArea)
}