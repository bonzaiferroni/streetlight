package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Entity
import streetlight.model.data.EntityFeed
import streetlight.model.data.EventTag
import streetlight.model.data.LocationId
import streetlight.model.data.MapQuery
import streetlight.model.data.writeMapQuery

/** The calls of `Api.EarthNode`. */
class EarthClient(private val transport: ApiTransport) {
    suspend fun inflate(locationId: LocationId, tag: EventTag?, searchText: String?): Outcome<List<Entity>> =
        transport.getApi(Api.EarthNode.Inflate) {
            writeParam(it.locationIdParam, locationId)
            tag?.let { tag -> writeParam(it.tagParam, tag.ordinal) }
            searchText?.let { text -> writeParam(it.searchParam, text) }
        }
    suspend fun readMapEntities(query: MapQuery): Outcome<EntityFeed> =
        transport.getApi(Api.EarthNode.Query) { it.writeMapQuery(query) }
}
