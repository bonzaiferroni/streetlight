package streetlight.web.model

import koala.model.Toaster
import kampfire.model.Messenger
import kampfire.model.Url
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.DropWhileBusy
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.CapabilityStatus
import streetlight.model.data.SellerStatus
import streetlight.model.data.SellerOnboarding
import streetlight.model.io.SellerClient
import web.window.window

/**
 * Reads the signed-in star's seller account and sends them to the provider's onboarding to create or finish it.
 *
 * [openUrl] leaves the app for the onboarding link.
 */
class SellerConnector(
    private val scope: CoroutineScope,
    private val api: SellerClient,
    private val toaster: Toaster,
    private val openUrl: (Url) -> Unit = { window.location.assign(it.value) },
) {
    private val onboarding = DropWhileBusy()
    private val state = storeOf(SellerConnectorState())
    val stateNow get() = state.now

    val stageFlow = state.tapOf { it.stage }

    init {
        scope.launch("read seller status") {
            val status = api.readStatus().toDataOr(toaster) { return@launch }
            state.set { copy(status = status, isLoaded = true) }
        }
    }

    /**
     * Opens the provider's onboarding, creating the seller account first when there is none. A call while one is
     * under way is dropped.
     */
    fun onboard(messenger: Messenger) {
        onboarding.launch(scope, ::onboard.name) {
            messenger.deliverSending()
            val url = api.onboard(SellerOnboarding(stateNow.country)).toDataOr(messenger) { return@launch }
            openUrl(url)
        }
    }
}

data class SellerConnectorState(
    val status: SellerStatus? = null,
    val isLoaded: Boolean = false,
    val country: String = "US",
) {
    val stage get() = when {
        !isLoaded -> SellerStage.Loading
        status == null -> SellerStage.NotStarted
        status.chargeStatus == CapabilityStatus.Active -> SellerStage.Ready
        status.isAwaitingSeller -> SellerStage.AwaitingSeller
        status.chargeStatus == CapabilityStatus.Pending -> SellerStage.UnderReview
        else -> SellerStage.Restricted
    }
}

/** The point a star has reached in becoming a seller. */
enum class SellerStage {
    Loading,
    NotStarted,
    AwaitingSeller,
    UnderReview,
    Restricted,
    Ready,
}
