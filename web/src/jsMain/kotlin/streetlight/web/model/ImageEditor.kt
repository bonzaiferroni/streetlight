package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.PrintLnMessenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.Image
import koala.model.MutableField
import koala.toImage
import streetlight.web.io.ApiClient

class ImageEditor(
    val imageField: MutableField<Image?>,
    private val api: ApiClient,
) {
    // val stateNow get() = state.now
    // val stateFlow = state.flow

    // val imageField = state.mutableFieldOf({ it.image }) { copy(image = it) }

    suspend fun finalizeImage(messenger: Messenger? = null) {
        val url = imageField.now?.url?.takeIf { it.isBlob } ?: return
        println("uploading image: $url")
        messenger?.deliver(UIMessage("Uploading image...", UIMessageType.Working))
        val image = api.uploadImageBlob(url).handleResponse(messenger ?: PrintLnMessenger)?.toImage() ?: return
        imageField.set(image)
    }
}

data class ImageEditorState(
    val image: Image?,
)