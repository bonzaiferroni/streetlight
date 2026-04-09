package streetlight.model.data

import kampfire.api.TableId
import kampfire.model.ImageSize
import kampfire.model.Url
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class UploadFile(
    val uploadFileId: UploadFileId,
    val starId: StarId?,
    val url: Url,
    val fileType: FileType,
    val size: ImageSize?,
    val fileFormat: FileFormat,
    val storage: StorageType,
    val createdAt: Instant
)

@JvmInline
@Serializable
value class UploadFileId(override val value: String) : TableId<String>, ProjectId {
    companion object { fun random() = UploadFileId(randomUuidString())}
}

@Serializable
enum class FileType {
    Image
}

@Serializable
enum class FileFormat(val ext: String) {
    JPEG("jpg"), PNG("png"), GIF("gif"), WEBP("webp"), BMP("bmp");

    val contentType get() = "image/$ext"
}

@Serializable
enum class StorageType {
    Local,
    S3,
}