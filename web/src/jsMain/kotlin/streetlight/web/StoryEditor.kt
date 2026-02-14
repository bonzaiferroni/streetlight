package streetlight.web

import koala.model.BrowserModel
import koala.model.mapDistinct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StoryEditor(
    scope: CoroutineScope,
    private val client: ClientContext,
): BrowserModel<StoryEditorState>(StoryEditorState(), scope) {

    val urlFlow = stateFlow.mapDistinct { it.url }

    fun setUrl(value: String) {
        setState { it.copy(url = value) }
    }

    fun readUrl() {
        viewModelScope.launch {
            val msg = client.api.readStoryUrl(stateNow.url)
            console.log(msg)
        }
    }
}

data class StoryEditorState(
    val url: String = "",
)