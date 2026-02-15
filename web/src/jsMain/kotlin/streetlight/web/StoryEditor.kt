package streetlight.web

import koala.dom.UIMessage
import koala.model.BrowserModel
import koala.model.mapDistinct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.StoryId
import streetlight.model.data.StoryUpdate

class StoryEditor(
    scope: CoroutineScope,
    private val client: ClientContext,
): BrowserModel<StoryEditorState>(StoryEditorState(), scope) {

    val infoUrlFlow = stateFlow.mapDistinct { it.story.infoUrl ?: "" }
    val headlineFlow = stateFlow.mapDistinct { it.story.headline }
    val imageUrlFlow = stateFlow.mapDistinct { it.story.imageUrl ?: "" }
    val descriptionFlow = stateFlow.mapDistinct { it.story.description }
    val locationFlow = stateFlow.mapDistinct { it.story.location }
    val postedAtFlow = stateFlow.mapDistinct { it.story.postedAt }
    
    val storyNow get() = stateNow.story

    fun initStory(storyId: StoryId?) {
    }

    fun setUrl(value: String) {
        setStory { it.copy(infoUrl = value) }
    }

    fun setHeadline(value: String) {
        setStory { it.copy(headline = value) }
    }

    fun setDescription(value: String) {
        setStory { it.copy(description = value) }
    }

    fun readUrl() {
        val url = storyNow.infoUrl?.takeIf { it.startsWith("http") } ?: return
        viewModelScope.launch {
            val parse = client.api.readStoryUrl(url)
            setStory { parse?.toStoryUpdate() ?: storyNow }
        }
    }
    
    private fun setStory(toNewState: (StoryUpdate) -> StoryUpdate) {
        setState { it.copy(story = toNewState(storyNow))}
    }
}

data class StoryEditorState(
    val storyId: StoryId? = null,
    val story: StoryUpdate = StoryUpdate(),
    val message: UIMessage? = null,
)
