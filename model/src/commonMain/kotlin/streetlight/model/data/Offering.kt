package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.toMarkdown
import koala.Image
import koala.model.RouteContent
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/**
 * Something a star offers for sale, at [priceCents] in [currency].
 *
 * [platformCut] is the share of each sale the star gives Streetlight, in basis points: 500 is 5%.
 */
@Serializable
data class Offering(
    val offeringId: OfferingId,
    val title: String,
    val priceCents: Int,
    val currency: String,
    val platformCut: Int,
    val isPublic: Boolean,
    override val description: Markdown?,
    val tags: List<OfferingTag>?,
    override val image: Image?,
): Entity, RouteContent {
    override val label get() = title
    override val body get() = description ?: priceLabel.toMarkdown()
    override val geoPoint get() = null
    override val markerId get() = offeringId.toString()
}

/** The price of this offering, as `12.50 USD`. */
val Offering.priceLabel get() = "${priceCents.toCentsLabel()} ${currency.uppercase()}"

@JvmInline
@Serializable
value class OfferingId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}
