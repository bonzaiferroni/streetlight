package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.PrintLnMessenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.toDataOr
import koala.Image
import kampfire.model.MutableTap
import streetlight.model.data.ImageTarget
import streetlight.web.io.ApiClient

/** Uploads the image in [imageField] when it is still a local blob, at the sizes of [target]. */
class ImageEditor(
    val imageField: MutableTap<Image?>,
    private val target: ImageTarget,
    private val api: ApiClient,
) {
    // td: add meta

    /** Uploads a blob image and replaces it with the stored one. Returns `false` when the upload fails. */
    suspend fun finalizeImage(messenger: Messenger? = null): Boolean {
        val blobImage = imageField.now?.takeIf { it.url.isBlob } ?: return true
        val uploadedImage = uploadImage(blobImage, target, messenger, api) ?: return false
        imageField.set(uploadedImage)
        return true
    }
}

/** Uploads the blob [image] for [target], reporting to [messenger], and returns the stored image or `null`. */
suspend fun uploadImage(image: Image, target: ImageTarget, messenger: Messenger?, api: ApiClient): Image? {
    messenger?.deliver(UIMessage("Uploading image...", UIMessageType.Working))
    return uploadImage(image, target, api).toDataOr(messenger ?: PrintLnMessenger, "Image uploaded.") { return null }
}

/** Uploads the blob [image] for [target]; throws when it is not a blob. */
suspend fun uploadImage(image: Image, target: ImageTarget, api: ApiClient): Outcome<Image> {
    if (!image.url.isBlob) error("is not a blob")
    println("uploading image: $image")
    return api.user.uploadImageBlob(image, target)
}