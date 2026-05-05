package streetlight.web.model

import kampfire.model.Ok
import kampfire.model.Problem
import koala.dom.UIMessage
import koala.dom.set
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.ContentEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyId
import streetlight.model.data.PostId
import streetlight.web.io.handleResponse
import streetlight.web.ui.ViewModel
import streetlight.web.ui.api

class ContentPoster(
    private val scope: CoroutineScope,
    override val app: Streetlight,
    private val galaxy: Galaxy,
): ViewModel {

    val message = storeOf(UIMessage())

    // private val state = storeOf(ContentPosterState())
    // val stateFlow = state.flow
    // val stateNow get() = state.now

    val editor = ContentEditor(scope, app, ContentEdit(null, galaxy.galaxyId))

    init {
        scope.launch {
            editor.contentFlow.mapDistinct { it.invalidMessage }.collect {
                message.set(it ?: "Looks good.")
            }
        }
    }

    fun submitPost() {
        var content = editor.contentNow.takeIf { it.isValid } ?: return
        scope.launch {
            val blobUrl = content.imageRef?.takeIf { it.isBlob }
            content = blobUrl?.let {
                val refUrl = api.uploadImage(blobUrl)
                if (refUrl == null) {
                    message.set("Unable to upload image.")
                    return@launch
                }

                content.copy(imageRef = refUrl)
            } ?: content

            api.createPost(content).handleResponse(toaster::toast) {
                app.stagePostAndGo(it, galaxy)
            }
        }
    }
}