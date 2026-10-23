package streetlight.web.io

import kampfire.model.HttpProblem
import kampfire.model.Ok
import kampfire.model.Outcome
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.OfferingId
import streetlight.model.data.SkillId
import kotlin.uuid.Uuid

/** Holds the star's [offerings] in memory, as the server would. */
class TestOfferingClient(
    offerings: List<Offering> = emptyList(),
): OfferingClient {
    val offerings = offerings.toMutableList()

    override suspend fun readMine(): Outcome<List<Offering>> = Ok(offerings.toList())

    override suspend fun readOffering(offeringId: OfferingId): Outcome<Offering> =
        offerings.firstOrNull { it.offeringId == offeringId }?.let { Ok(it) } ?: HttpProblem.NotFound

    override suspend fun create(edit: OfferingEdit): Outcome<Offering> {
        val offering = edit.toOffering(OfferingId(Uuid.random()))
        offerings.add(offering)
        return Ok(offering)
    }

    override suspend fun update(edit: OfferingEdit): Outcome<Offering> {
        val offeringId = edit.offeringId ?: error("offering id not found")
        val index = offerings.indexOfFirst { it.offeringId == offeringId }.takeIf { it >= 0 }
            ?: error("offering not found: $offeringId")
        val offering = edit.toOffering(offeringId)
        offerings[index] = offering
        return Ok(offering)
    }

    override suspend fun readSkillIds(offeringId: OfferingId): Outcome<List<SkillId>> = Ok(emptyList())
}

private fun OfferingEdit.toOffering(offeringId: OfferingId) = Offering(
    offeringId = offeringId,
    title = title ?: error("title not found"),
    priceCents = priceCents ?: error("price not found"),
    currency = currency ?: "usd",
    platformCut = platformCut,
    isPublic = false,
    description = description,
    tags = tags.takeIf { it.isNotEmpty() },
    image = image,
)
