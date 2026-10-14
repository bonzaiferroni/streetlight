package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit

class SellerConfig(
    private val scope: CoroutineScope
) {
    private val state = storeOf(SellerConfigState())

    val offeringEditState = state.tapOf { it.offeringEdit }
    val offeringsState = state.tapOf { it.offerings }
}

data class SellerConfigState(
    val offeringEdit: OfferingEdit? = null,
    val offerings: List<Offering>? = null,
)