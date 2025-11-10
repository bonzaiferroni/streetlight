package streetlight.app.io

import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.Street
import streetlight.model.data.StreetId
import streetlight.model.data.NewStreet
import streetlight.model.mockDb

interface StreetRepository {
    suspend fun readAll(): List<Street>?
    suspend fun create(street: NewStreet): StreetId?
}

class StreetApiClient(
    private val client: NeoApiClient
): StreetRepository {
    override suspend fun readAll() = client.request(Api.StreetFeed)
    override suspend fun create(street: NewStreet) = client.request(Api.StreetFeed.Create, street)
}

class StreetMockClient: StreetRepository {
    override suspend fun readAll() = mockDb.streets

    override suspend fun create(street: NewStreet): StreetId? = TODO("Not yet implemented")
}