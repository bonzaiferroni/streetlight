package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.LocationEdit

/** The onboarding of a star. */
class LocationOnboarder(private val scope: CoroutineScope) {
    private val state = storeOf(LocationOnboarderState())

    val nameState = state.mutableTapOf({ it.name }) { copy(name = it) }
    val addressState = state.mutableTapOf({ it.address }) { copy(address = it) }
}

data class LocationOnboarderState(
    val name: String = "",
    val address: String = "",
)
