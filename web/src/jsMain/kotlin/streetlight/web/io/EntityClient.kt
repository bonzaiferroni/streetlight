package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef

/** The calls of `Api.Entities`. */
interface EntityClient {
    suspend fun readEntity(ref: EntityRef): Outcome<Entity>
}

class BrowserEntityClient(private val client: FetchClient): EntityClient {
    override suspend fun readEntity(ref: EntityRef) = client.getApi(Api.Entities.Read) {
        writeParam(it.type, ref.type)
        writeParam(it.slug, ref.slug.value)
    }
}
