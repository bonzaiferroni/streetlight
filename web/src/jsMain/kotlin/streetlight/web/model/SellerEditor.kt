package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Url
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.DropWhileBusy
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.CapabilityStatus
import streetlight.model.data.Seller
import streetlight.model.data.SellerOnboarding
import streetlight.web.io.SellerClient
import web.window.window

/**
 * Reads the signed-in star's seller account and sends them to the provider's onboarding to create or finish it.
 *
 * [openUrl] leaves the app for the onboarding link.
 */
class SellerEditor(
    private val scope: CoroutineScope,
    private val api: SellerClient,
    private val toaster: Toaster,
    private val openUrl: (Url) -> Unit = { window.location.assign(it.value) },
) {
    private val onboarding = DropWhileBusy()
    private val state = storeOf(SellerEditorState())
    val stateNow get() = state.now

    val stageFlow = state.tapOf { it.stage }

    init {
        scope.launch("read seller status") {
            val seller = api.readStatus().toDataOr(toaster) { return@launch }
            state.set { copy(seller = seller, isLoaded = true) }
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

data class SellerEditorState(
    val seller: Seller? = null,
    val isLoaded: Boolean = false,
    val country: String = "US",
) {
    val stage get() = when {
        !isLoaded -> SellerStage.Loading
        seller == null -> SellerStage.NotStarted
        seller.chargeStatus == CapabilityStatus.Active -> SellerStage.Ready
        seller.isAwaitingSeller -> SellerStage.AwaitingSeller
        seller.chargeStatus == CapabilityStatus.Pending -> SellerStage.UnderReview
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
