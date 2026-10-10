package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef

class TestEntityClient: EntityClient {
    override suspend fun readEntity(ref: EntityRef): Outcome<Entity> = TODO()
}
