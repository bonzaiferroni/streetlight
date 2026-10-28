package streetlight.compose.model

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import kampfire.model.toUrl
import koala.Image
import streetlight.model.data.ImageTarget
import streetlight.model.io.ApiClient

/**
 * An image of an edit, at the sizes of [target]: the stored one, or one picked from the device and held until the edit
 * is saved, when it is uploaded. A submodel, held by the editor of anything with an image, as `LocationEditor` holds
 * one. The cousin of the web's `ImageEditor`.
 */
class ImageEditor(private val target: ImageTarget, private val api: ApiClient) {
    private val store = storeOf(ImageEditorState())

    val imageState = store.tapOf { it.image }
    val pickedState = store.tapOf { it.picked }

    /** Starts from the stored [image], if any, with nothing picked. */
    fun setImage(image: Image?) = store.set { ImageEditorState(image) }

    /** Holds the image of [bytes], picked from the file [name], in place of the stored one. */
    fun pick(name: String, bytes: ByteArray) = store.set { copy(picked = PickedImage(name, bytes)) }

    /** Leaves the edit with no image. */
    fun clear() = store.set { ImageEditorState() }

    /** Uploads a picked image and keeps the stored one it becomes; the image as it stands when nothing was picked. */
    suspend fun finalizeImage(): Outcome<Image?> {
        val picked = store.now.picked ?: return Ok(store.now.image)
        val meta = Image(url = picked.name.toUrl(), name = picked.name)
        val image = api.user.uploadImage(meta, picked.bytes, target).toDataOr { return it }
        store.set { ImageEditorState(image) }
        return Ok(image)
    }
}

/** The stored [image] of an [ImageEditor], and the one [picked] to take its place. */
data class ImageEditorState(
    val image: Image? = null,
    val picked: PickedImage? = null,
)

/** An image picked from the device, its file's [name] and [bytes], not yet uploaded. */
class PickedImage(val name: String, val bytes: ByteArray)
