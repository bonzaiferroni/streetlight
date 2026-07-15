package streetlight.web.model

import kampfire.model.handleResponse
import koala.model.tap
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

    val feedFlow = stateFlow.tap { it.feed }
    val editFlow = stateFlow.tap { it.edit }
    val textFlow = editFlow.tap { it.text }

    init {
        scope.launch {
            refreshFeedback()
        }
    }

    fun setText(value: String) = setEdit { it.copy(text = value) }

    fun sendFeedback() {
        val edit = stateNow.edit.takeIf { it.isValid } ?: return
        scope.launch {
            val isSuccess = api.createFeedback(edit).handleResponse(toaster) ?: false
            if (isSuccess) refreshFeedback()
        }
    }

    private fun setEdit(block: (FeedbackEdit) -> FeedbackEdit) = state.set { it.copy(edit = block(it.edit)) }

    private suspend fun refreshFeedback() {
        val list = api.feedFeedback().handleResponse(toaster) ?: emptyList()
        state.set { it.copy(feed = list) }
    }
}

data class FeedbackDeskState(
    val feed: List<Feedback> = emptyList(),
    val edit: FeedbackEdit = FeedbackEdit.Empty,
)