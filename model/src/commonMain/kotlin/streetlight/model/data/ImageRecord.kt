package streetlight.model.data

import koala.Image
import koala.ImageId
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class ImageRecord(
    val imageId: ImageId,
    val starId: StarId?,
    val format: ImageFormat,
    val image: Image,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
enum class FileType {
    Image
}

@Serializable
enum class ImageFormat(val ext: String) {
    JPEG("jpg"), PNG("png"), GIF("gif"), WEBP("webp"), BMP("bmp");

    val contentType get() = "image/$ext"
}