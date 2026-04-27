package streetlight.web.model

import koala.dom.UIMessage
import koala.model.BrowserModel
import koala.model.GeoMap
import koala.model.mapDistinct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.ProtoPostId
import streetlight.model.data.ProtoPostUpdate

class PostEditor(
    scope: CoroutineScope,
    private val client: ClientContext,
    private val geoMap: GeoMap,
): BrowserModel<PostEditorState>(PostEditorState(), scope) {

    val infoUrlFlow = stateFlow.mapDistinct { it.story.infoUrl ?: "" }
    val headlineFlow = stateFlow.mapDistinct { it.story.title }
    val imageUrlFlow = stateFlow.mapDistinct { it.story.imageUrl }
    val descriptionFlow = stateFlow.mapDistinct { it.story.description }
    val locationFlow = stateFlow.mapDistinct { it.story.location }
    val postedAtFlow = stateFlow.mapDistinct { it.story.postedAt }
    
    val storyNow get() = stateNow.story

    fun initStory(postId: ProtoPostId?) {
    }

    fun setUrl(value: String) {
        setStory { it.copy(infoUrl = value) }
    }

    fun setHeadline(value: String) {
        setStory { it.copy(title = value) }
    }

    fun setDescription(value: String) {
        setStory { it.copy(description = value) }
    }

    fun readUrl() {
        val url = storyNow.infoUrl?.takeIf { it.startsWith("http") } ?: return
        scope.launch {
            val parse = client.api.readStoryUrl(url)
            parse?.geoPoint?.let {
                geoMap.panMap(it)
            }
            setStory { parse?.toStoryUpdate() ?: storyNow }
        }
    }
    
    private fun setStory(toNewState: (ProtoPostUpdate) -> ProtoPostUpdate) {
        setState { it.copy(story = toNewState(storyNow))}
    }
}

data class PostEditorState(
    val postId: ProtoPostId? = null,
    val story: ProtoPostUpdate = ProtoPostUpdate(),
    val message: UIMessage? = null,
)
