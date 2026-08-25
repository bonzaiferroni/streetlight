package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.Outcome
import kampfire.model.PrintLnMessenger
import kampfire.model.UIMessage
import kampfire.model.UIMessageType
import kampfire.model.toDataOr
import koala.Image
import koala.model.MutableTap
import streetlight.web.io.ApiClient

class ImageEditor(
    val imageField: MutableTap<Image?>,
    private val api: ApiClient,
) {
    // td: add meta

    suspend fun finalizeImage(messenger: Messenger? = null) {
        val blobImage = imageField.now?.takeIf { it.url.isBlob } ?: return
        val uploadedImage = uploadImage(blobImage, messenger, api) ?: return
        imageField.set(uploadedImage)
    }
}

suspend fun uploadImage(image: Image, messenger: Messenger?, api: ApiClient): Image? {
    messenger?.deliver(UIMessage("Uploading image...", UIMessageType.Working))
    return uploadImage(image, api).toDataOr(messenger ?: PrintLnMessenger, "Image uploaded.") { return null }
}

suspend fun uploadImage(image: Image, api: ApiClient): Outcome<Image> {
    if (!image.url.isBlob) error("is not a blob")
    println("uploading image: $image")
    return api.uploadImageBlob(image)
}