package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.EntityFeed
import streetlight.model.data.LocationId

/** The calls of `Api.EarthNode`. */
interface EarthClient {
    suspend fun inflate(locationIds: List<LocationId>): Outcome<EntityFeed>
}

class BrowserEarthClient(private val client: FetchClient): EarthClient {
    override suspend fun inflate(locationIds: List<LocationId>) =
        client.getApi(Api.EarthNode.Inflate) {
            writeParam(it.locationIdsParam, locationIds)
        }
}
