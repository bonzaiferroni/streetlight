package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.toValidityCheck
import koala.Image
import kotlinx.serialization.Serializable

/** The fields of an offering a form sends. */
@Serializable
data class OfferingEdit(
    val offeringId: OfferingId? = null,
    val title: String? = null,
    val description: Markdown? = null,
    val tags: List<OfferingTag> = emptyList(),
    val image: Image? = null,
    val priceCents: Int? = null,
    val currency: String? = null,
    val platformCut: Int = 0,
) {
    val validity by lazy {
        buildSet {
            if (title.isNullOrBlank()) add(OfferingProperty.Title)
            if (priceCents == null) add(OfferingProperty.Price)
        }.toValidityCheck()
    }
}

/** The keys of the parts of an [OfferingEdit] its validity checks. */
object OfferingProperty {
    val Title = "title"
    val Price = "price"
}

/** An edit of this offering, starting from its current values. */
fun Offering.toEdit() = OfferingEdit(
    offeringId = offeringId,
    title = title,
    description = description,
    tags = tags ?: emptyList(),
    image = image,
    priceCents = priceCents,
    currency = currency,
    platformCut = platformCut,
)
