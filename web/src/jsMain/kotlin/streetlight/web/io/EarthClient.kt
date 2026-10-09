package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Entity
import streetlight.model.data.EntityFeed
import streetlight.model.data.EventTag
import streetlight.model.data.LocationId
import streetlight.model.data.MapQuery
import streetlight.model.data.writeMapQuery

/** The calls of `Api.EarthNode`. */
interface EarthClient {
    suspend fun inflate(locationId: LocationId, tag: EventTag?): Outcome<List<Entity>>
    suspend fun readMapEntities(query: MapQuery): Outcome<EntityFeed>
}

class BrowserEarthClient(private val client: FetchClient): EarthClient {
    override suspend fun inflate(locationId: LocationId, tag: EventTag?) =
        client.getApi(Api.EarthNode.Inflate) {
            writeParam(it.locationIdParam, locationId)
            tag?.let { tag -> writeParam(it.tagParam, tag.ordinal) }
        }
    override suspend fun readMapEntities(query: MapQuery) =
        client.getApi(Api.EarthNode.Query) { it.writeMapQuery(query) }
}
