package streetlight.web.model

import kampfire.model.handleResponse
import koala.dom.launch
import koala.model.dedup
import koala.model.fieldOf
import koala.model.mutableFieldOf
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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

    val feedFlow = state.fieldOf { it.feed }
    val editField = state.mutableFieldOf({ it.edit }) { copy(edit = it) }
    val textField = editField.mutableFieldOf({ it.text }) { copy(text = it) }

    init {
        scope.launch(::refreshFeedback) {
            refreshFeedback()
        }
    }

    fun sendFeedback() {
        val edit = stateNow.edit.takeIf { it.isValid } ?: return
        scope.launch(::sendFeedback) {
            val isSuccess = api.createFeedback(edit).handleResponse(toaster) ?: false
            if (isSuccess) refreshFeedback()
        }
    }

    private suspend fun refreshFeedback() {
        val list = api.feedFeedback().handleResponse(toaster) ?: emptyList()
        state.set { copy(feed = list) }
    }
}

data class FeedbackDeskState(
    val feed: List<Feedback> = emptyList(),
    val edit: FeedbackEdit = FeedbackEdit.Empty,
)