package streetlight.model.data

import kampfire.api.TableId
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class UserFile(
    val userFileId: UserFileId,
    val userId: UserId,
    val url: String,
    val fileType: FileType,
    val fileUse: FileUse,
    val fileFormat: FileFormat,
)

@JvmInline
@Serializable
value class UserFileId(override val value: String) : TableId<String>, ProjectId {
    companion object { fun random() = UserFileId(randomUuidString())}
}

enum class FileType {
    Image
}

enum class FileUse {
    ProfileImage,
    EventImage,
    LocationImage,
}

enum class FileFormat(val ext: String) {
    JPEG("jpg"), PNG("png"), GIF("gif"), WEBP("webp"), BMP("bmp")
}