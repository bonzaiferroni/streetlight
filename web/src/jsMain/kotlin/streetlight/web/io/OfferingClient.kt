package streetlight.web.io

import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.OfferingId
import streetlight.model.data.SkillId

/** The calls of `Api.Offerings`. */
interface OfferingClient {
    suspend fun readMine(): Outcome<List<Offering>>
    suspend fun readOffering(offeringId: OfferingId): Outcome<Offering>
    suspend fun create(edit: OfferingEdit): Outcome<Offering>
    suspend fun update(edit: OfferingEdit): Outcome<Offering>
    suspend fun readSkillIds(offeringId: OfferingId): Outcome<List<SkillId>>
}

class BrowserOfferingClient(private val client: FetchClient): OfferingClient {
    override suspend fun readMine() = client.getApi(Api.Offerings.ReadMine)
    override suspend fun readOffering(offeringId: OfferingId) = client.getApi(Api.Offerings.Read, offeringId)
    override suspend fun create(edit: OfferingEdit) = client.postApi(Api.Offerings.Create, edit)
    override suspend fun update(edit: OfferingEdit) = client.postApi(Api.Offerings.Update, edit)
    override suspend fun readSkillIds(offeringId: OfferingId) = client.getApi(Api.Offerings.ReadSkillIds, offeringId)
}
