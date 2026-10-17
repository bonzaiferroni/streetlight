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
            if (priceCents == null || priceCents < 0) add(OfferingProperty.Price)
            if (platformCut !in 0..MAX_PLATFORM_CUT) add(OfferingProperty.PlatformCut)
        }.toValidityCheck()
    }
}

/** The keys of the parts of an [OfferingEdit] its validity checks. */
object OfferingProperty {
    val Title = "title"
    val Price = "price"
    val PlatformCut = "platform cut"
}

/** The largest [OfferingEdit.platformCut], in basis points: the whole sale. */
const val MAX_PLATFORM_CUT = 10_000

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

/**
 * The whole hundredths in this text, as cents from `12.50` or basis points from `5`, or `null` when it is not a
 * number with at most two decimal places.
 */
fun String.toCentsOrNull(): Int? {
    val text = trim()
    val whole = text.substringBefore('.')
    val fraction = text.substringAfter('.', "")
    if (whole.isEmpty() && fraction.isEmpty()) return null
    if (!whole.all { it.isDigit() } || !fraction.all { it.isDigit() } || fraction.length > 2) return null
    val wholeValue = whole.ifEmpty { "0" }.toIntOrNull() ?: return null
    return wholeValue * 100 + fraction.padEnd(2, '0').toInt()
}

/** These cents as text with two decimal places, as `12.50`. */
fun Int.toCentsLabel() = "${this / 100}.${(this % 100).toString().padStart(2, '0')}"

/** These basis points as a percent with no trailing zeros, as `5` or `2.5`. */
fun Int.toPercentLabel(): String {
    val fraction = (this % 100).toString().padStart(2, '0').trimEnd('0')
    return if (fraction.isEmpty()) "${this / 100}" else "${this / 100}.$fraction"
}
