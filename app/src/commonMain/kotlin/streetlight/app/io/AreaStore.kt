package streetlight.app.io

import pondui.io.ApiClient
import pondui.io.NeoApiClient
import pondui.io.globalNeoApiClient
import streetlight.model.Api
import streetlight.model.data.NewArea

class AreaStore(
    private val client: NeoApiClient = globalNeoApiClient
) {
    suspend fun readAll() = client.request(Api.Areas)
    suspend fun createArea(newArea: NewArea) = client.request(Api.Areas.Create, newArea)
}