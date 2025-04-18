package streetlight.app.io

import pondui.io.ApiStore
import streetlight.model.Api
import streetlight.model.data.NewArea

class AreaStore : ApiStore() {
    suspend fun readAll() = client.get(Api.Areas)
    suspend fun createArea(newArea: NewArea) = client.post(Api.Areas.Create, newArea)
}