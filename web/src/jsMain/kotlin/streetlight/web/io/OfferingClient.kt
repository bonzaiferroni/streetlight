package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit

/** The calls of `Api.Offerings`. */
interface OfferingClient {
    suspend fun readMine(): Outcome<List<Offering>>
    suspend fun create(edit: OfferingEdit): Outcome<Offering>
    suspend fun update(edit: OfferingEdit): Outcome<Offering>
}

class BrowserOfferingClient(private val client: FetchClient): OfferingClient {
    override suspend fun readMine() = client.getApi(Api.Offerings.ReadMine)
    override suspend fun create(edit: OfferingEdit) = client.postApi(Api.Offerings.Create, edit)
    override suspend fun update(edit: OfferingEdit) = client.postApi(Api.Offerings.Update, edit)
}
