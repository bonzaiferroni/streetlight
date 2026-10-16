package streetlight.model.ui

import streetlight.model.data.OfferingId

data class OfferingRoute(val offeringId: OfferingId): RecordIdRoute {
    override val screen get() = Screen.Offering
    override val recordId get() = offeringId
    override val title get() = "Offering"
}
