package streetlight.web.model

import kampfire.model.getDataOrNull
import koala.Image
import koala.dom.MessageStore
import koala.model.mutableFieldOf
import koala.model.storeOf
import koala.toImage
import streetlight.web.io.ApiClient

class ImageEditor(
    initialImage: Image?,
    private val api: ApiClient,
) {
    private val state = storeOf(ImageEditorState(initialImage))
    val stateNow get() = state.now
    val stateFlow = state.flow

    val imageField = state.mutableFieldOf({ it.image }) { copy(image = it) }

    suspend fun finalizeImage(message: MessageStore? = null): Image? {
        val url = stateNow.image?.url?.takeIf { it.isBlob } ?: return stateNow.image
        message?.set("Uploading image...", true)
        val image = api.uploadImageBlob(url).getDataOrNull()?.toImage()
        if (image == null) {
            message?.receive("Unable to upload image.")
        }
        return image
    }
}

data class ImageEditorState(
    val image: Image?,
)