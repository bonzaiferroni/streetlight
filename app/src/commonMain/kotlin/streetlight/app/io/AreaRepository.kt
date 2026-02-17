package streetlight.app.io

import pondui.io.NeoApiClient
import streetlight.model.Api
import streetlight.model.data.Community
import streetlight.model.data.CommunityId
import streetlight.model.data.NewCommunity
import streetlight.model.mockDb

interface AreaRepository {
    suspend fun readAll(): List<Community>?
    suspend fun create(street: NewCommunity): CommunityId?
}

class AreaApiClient(
    private val client: NeoApiClient
): AreaRepository {
    override suspend fun readAll() = client.request(Api.StreetFeed)
    override suspend fun create(street: NewCommunity) = client.request(Api.StreetFeed.Create, street)
}

class AreaMockClient: AreaRepository {
    override suspend fun readAll() = mockDb.communities

    override suspend fun create(street: NewCommunity): CommunityId? = TODO("Not yet implemented")
}