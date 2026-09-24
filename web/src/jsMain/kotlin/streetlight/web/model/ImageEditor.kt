package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.PrintLnMessenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.toDataOr
import koala.Image
import kampfire.model.MutableTap
import streetlight.web.io.ApiClient

/** Uploads the image in [imageField] when it is still a local blob. */
class ImageEditor(
    val imageField: MutableTap<Image?>,
    private val api: ApiClient,
) {
    // td: add meta

    /** Uploads a blob image and replaces it with the stored one. Returns `false` when the upload fails. */
    suspend fun finalizeImage(messenger: Messenger? = null): Boolean {
        val blobImage = imageField.now?.takeIf { it.url.isBlob } ?: return true
        val uploadedImage = uploadImage(blobImage, messenger, api) ?: return false
        imageField.set(uploadedImage)
        return true
    }
}

/** Uploads the blob [image], reporting to [messenger], and returns the stored image or `null`. */
suspend fun uploadImage(image: Image, messenger: Messenger?, api: ApiClient): Image? {
    messenger?.deliver(UIMessage("Uploading image...", UIMessageType.Working))
    return uploadImage(image, api).toDataOr(messenger ?: PrintLnMessenger, "Image uploaded.") { return null }
}

/** Uploads the blob [image]; throws when it is not a blob. */
suspend fun uploadImage(image: Image, api: ApiClient): Outcome<Image> {
    if (!image.url.isBlob) error("is not a blob")
    println("uploading image: $image")
    return api.user.uploadImageBlob(image)
}