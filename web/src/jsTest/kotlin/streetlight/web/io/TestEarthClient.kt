package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.EntityFeed
import streetlight.model.data.LocationId
import streetlight.model.data.MapQuery

class TestEarthClient: EarthClient {
    override suspend fun inflate(locationIds: List<LocationId>): Outcome<EntityFeed> = TODO()
    override suspend fun readMapEntities(query: MapQuery): Outcome<EntityFeed> = TODO()
}
