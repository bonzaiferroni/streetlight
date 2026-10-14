package streetlight.model.data

import kampfire.model.Labeled

/** The kind of work an offering delivers. */
enum class OfferingTag(override val label: String): Labeled {
    CreateLocation("Create Location"),
    ManageLocation("Manage Location"),
}
