package streetlight.model.data

import kampfire.api.TableId
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class UploadFile(
    val uploadFileId: UploadFileId,
    val userId: UserId?,
    val url: String,
    val fileType: FileType,
    val fileUse: FileUse,
    val fileFormat: FileFormat,
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
enum class FileUse {
    FullImage,
    ThumbImage,
}

@Serializable
enum class FileFormat(val ext: String) {
    JPEG("jpg"), PNG("png"), GIF("gif"), WEBP("webp"), BMP("bmp");

    val contentType get() = "image/$ext"
}

@Serializable
data class UserFileRequest(
    val fileUse: FileUse,
    val count: Int = 10
)