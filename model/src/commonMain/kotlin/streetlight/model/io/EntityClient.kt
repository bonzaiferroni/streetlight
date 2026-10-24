package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef

/** The calls of `Api.Entities`. */
class EntityClient(private val transport: ApiTransport) {
    suspend fun readEntity(ref: EntityRef): Outcome<Entity> = transport.getApi(Api.Entities.Read) {
        writeParam(it.type, ref.type)
        writeParam(it.slug, ref.slug.value)
    }
}
