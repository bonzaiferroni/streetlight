package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.PrintLnMessenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.Url
import kampfire.model.handleResponse
import koala.Image
import koala.model.MutableTap
import koala.toImage
import streetlight.web.io.ApiClient

class ImageEditor(
    val imageField: MutableTap<Image?>,
    private val api: ApiClient,
) {
    // val stateNow get() = state.now
    // val stateFlow = state.flow

    // val imageField = state.mutableFieldOf({ it.image }) { copy(image = it) }

    suspend fun finalizeImage(messenger: Messenger? = null) {
        val image = uploadImage(imageField.now?.url, messenger, api) ?: return
        imageField.set(image)
    }
}

data class ImageEditorState(
    val image: Image?,
)

suspend fun uploadImage(url: Url?, messenger: Messenger?, api: ApiClient): Image? {
    val url = url?.takeIf { it.isBlob } ?: return null
    println("uploading image: $url")
    messenger?.deliver(UIMessage("Uploading image...", UIMessageType.Working))
    return api.uploadImageBlob(url).handleResponse(messenger ?: PrintLnMessenger)
}