package streetlight.web.model

import kampfire.model.toDataOr
import koala.utils.launch
import koala.model.tapOf
import koala.model.mutableTapOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Feedback
import streetlight.model.data.FeedbackEdit
import streetlight.web.io.ApiClient

class FrontDesk(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(FeedbackDeskState())
    val stateNow get() = state.now
    val stateFlow get() = state.flow

    val feedFlow = state.tapOf { it.feed }
    val editField = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val textField = editField.mutableTapOf({ it.text }) { copy(text = it) }

    init {
        scope.launch(::refreshFeedback) {
            refreshFeedback()
        }
    }

    fun sendFeedback() {
        val edit = stateNow.edit.takeIf { it.isValid } ?: return
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

data class FeedbackDeskState(
    val feed: List<Feedback> = emptyList(),
    val edit: FeedbackEdit = FeedbackEdit.Empty,
)