package streetlight.model.data

import kampfire.model.Labeled

/** The trade of the star an offering comes from, as a developer or a performer. */
enum class OfferingTag(override val label: String): Labeled {
    Developer("Developer"),
    Performer("Performer"),
}
