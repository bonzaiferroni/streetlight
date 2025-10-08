package streetlight.app.io

import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.model.data.NewArea
import streetlight.model.mockDb

interface AreaRepository {
    suspend fun readAll(): List<Area>?
    suspend fun createArea(area: NewArea): AreaId?
}

class AreaApiClient(
    private val client: NeoApiClient
): AreaRepository {
    override suspend fun readAll() = client.request(Api.AreaFeed)
    override suspend fun createArea(area: NewArea) = client.request(Api.AreaFeed.Create, area)
}

class AreaMockClient: AreaRepository {
    override suspend fun readAll() = mockDb.areas

    override suspend fun createArea(area: NewArea): AreaId? = TODO("Not yet implemented")
}