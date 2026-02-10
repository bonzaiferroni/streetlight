package streetlight.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.*
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toUserId

object UserFileTable : UUIDTable("user_file") {
    val userId = reference("user_id", UserTable, ReferenceOption.CASCADE)
    val url = text("url")
    val fileType = enumeration<FileType>("file_type")
    val fileUse = enumeration<FileUse>("file_use")
    val fileFormat = enumeration<FileFormat>("file_format")
}

fun ResultRow.toUserFile() = UserFile(
    userFileId = toProjectId(UserFileTable.id),
    userId = toUserId(UserFileTable.userId),
    url = this[UserFileTable.url],
    fileType = this[UserFileTable.fileType],
    fileUse = this[UserFileTable.fileUse],
    fileFormat = this[UserFileTable.fileFormat],
)

fun UpdateBuilder<*>.writeFull(userFile: UserFile) {
    this[UserFileTable.id] = userFile.userFileId.toUUID()
    this[UserFileTable.userId] = userFile.userId.toUUID()
    writeUpdate(userFile)
}

fun UpdateBuilder<*>.writeUpdate(userFile: UserFile) {
    this[UserFileTable.url] = userFile.url
    this[UserFileTable.fileType] = userFile.fileType
    this[UserFileTable.fileUse] = userFile.fileUse
    this[UserFileTable.fileFormat] = userFile.fileFormat
}
