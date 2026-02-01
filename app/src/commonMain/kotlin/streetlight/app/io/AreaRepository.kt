package streetlight.app.io

import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.Area
import streetlight.model.data.AreaId
import streetlight.model.data.NewArea
import streetlight.model.mockDb

interface AreaRepository {
    suspend fun readAll(): List<Area>?
    suspend fun create(street: NewArea): AreaId?
}

class AreaApiClient(
    private val client: NeoApiClient
): AreaRepository {
    override suspend fun readAll() = client.request(Api.StreetFeed)
    override suspend fun create(street: NewArea) = client.request(Api.StreetFeed.Create, street)
}

class AreaMockClient: AreaRepository {
    override suspend fun readAll() = mockDb.communities

    override suspend fun create(street: NewArea): AreaId? = TODO("Not yet implemented")
}