package streetlight.model.io

import kampfire.api.ApiTransport
import kampfire.api.getApi
import kampfire.api.postApi
import kampfire.model.Outcome
import streetlight.model.Api
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.OfferingId
import streetlight.model.data.SkillId

/** The calls of `Api.Offerings`. */
class OfferingClient(private val transport: ApiTransport) {
    suspend fun readMine(): Outcome<List<Offering>> = transport.getApi(Api.Offerings.ReadMine)
    suspend fun readOffering(offeringId: OfferingId): Outcome<Offering> = transport.getApi(Api.Offerings.Read, offeringId)
    suspend fun create(edit: OfferingEdit): Outcome<Offering> = transport.postApi(Api.Offerings.Create, edit)
    suspend fun update(edit: OfferingEdit): Outcome<Offering> = transport.postApi(Api.Offerings.Update, edit)
    suspend fun readSkillIds(offeringId: OfferingId): Outcome<List<SkillId>> = transport.getApi(Api.Offerings.ReadSkillIds, offeringId)
}
