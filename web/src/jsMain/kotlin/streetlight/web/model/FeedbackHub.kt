package streetlight.web.model

import kampfire.model.toDataOr
import koala.utils.launch
import kampfire.model.tapOf
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackEdit
import streetlight.model.data.Platform
import streetlight.web.io.ApiClient
import web.device.devicePixelRatio
import web.navigator.navigator
import web.window.window

class FeedbackHub(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(FeedbackHubState(FeedbackEdit(platform = Platform.Web)))
    val stateNow get() = state.now

    val feedFlow = state.tapOf { it.feed }
    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val textState = editState.mutableTapOf({ it.text }) { copy(text = it) }
    val feedbackTypeState = editState.mutableTapOf({ it.feedbackType }) { copy(feedbackType = it) }
    val isPrivateState = editState.mutableTapOf({ it.isPrivate} ) { copy(isPrivate = it) }

    init {
        scope.launch(::refreshFeedback) {
            refreshFeedback()
        }
    }

    fun sendFeedback() {
        val edit = stateNow.edit.takeIf { it.isValid }?.copy(
            deviceAgent = readDeviceAgent()
        ) ?: return
        scope.launch(::sendFeedback) {
            api.createFeedback(edit).toDataOr(toaster) { return@launch }
            refreshFeedback()
        }
    }

    private suspend fun refreshFeedback() {
        val list = api.feedFeedback().toDataOr(toaster) { return }
        state.set { copy(feed = list) }
    }
}

data class FeedbackHubState(
    val edit: FeedbackEdit,
    val feed: List<Feedback> = emptyList(),
)

private fun readDeviceAgent() = listOf(
    navigator.userAgent,
    "viewport=${window.innerWidth}x${window.innerHeight}",
    "dpr=$devicePixelRatio",
).joinToString(" | ")