package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.OfferingId
import streetlight.model.data.toEdit
import streetlight.web.io.OfferingClient

/** The star's offerings, and the [OfferingEdit] staged for an [OfferingEditor], `null` while none is. */
class SellerConfig(
    private val scope: CoroutineScope,
    private val api: OfferingClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(SellerConfigState())

    val offeringEditState = state.tapOf { it.offeringEdit }
    val offeringsState = state.tapOf { it.offerings }

    init {
        scope.launch("read offerings") {
            val offerings = api.readMine().toDataOr(toaster) { return@launch }
            state.set { copy(offerings = offerings) }
        }
    }

    fun startNewOffering() = state.set { copy(offeringEdit = OfferingEdit()) }

    fun startOfferingEdit(offering: Offering) = state.set { copy(offeringEdit = offering.toEdit()) }

    fun cancelOfferingEdit() = state.set { copy(offeringEdit = null) }

    /** Puts [offering] in the list, in place of the one it updates or last when it is new, and ends the edit. */
    fun finishOfferingEdit(offering: Offering) = state.set {
        val offerings = offerings ?: emptyList()
        val isUpdate = offerings.any { it.offeringId == offering.offeringId }
        copy(
            offerings = if (isUpdate) offerings.map { if (it.offeringId == offering.offeringId) offering else it }
                else offerings + offering,
            offeringEdit = null,
        )
    }
}

data class SellerConfigState(
    val offeringEdit: OfferingEdit? = null,
    val offerings: List<Offering>? = null,
)
