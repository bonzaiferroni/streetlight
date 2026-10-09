package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.Entity
import streetlight.model.data.EntityFeed
import streetlight.model.data.EventTag
import streetlight.model.data.LocationId
import streetlight.model.data.MapQuery

class TestEarthClient: EarthClient {
    override suspend fun inflate(locationId: LocationId, tag: EventTag?, searchText: String?): Outcome<List<Entity>> = TODO()
    override suspend fun readMapEntities(query: MapQuery): Outcome<EntityFeed> = TODO()
}
