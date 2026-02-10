package streetlight.server.db.services

import kampfire.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.UserFile
import streetlight.model.data.UserFileId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.UserFileTable
import streetlight.server.db.tables.toUserFile
import streetlight.server.db.tables.writeFull

class UserFileTableDao : DbService() {

    suspend fun readUserFile(userFileId: UserFileId) = dbQuery {
        UserFileTable.read { it.id.eq(userFileId) }.firstOrNull()?.toUserFile()
    }

    suspend fun readUserFiles(userId: UserId) = dbQuery {
        UserFileTable.read { UserFileTable.userId.eq(userId) }.map { it.toUserFile() }
    }

    suspend fun create(userFile: UserFile): UserFileId = dbQuery {
        UserFileTable.insertAndGetId {
            it.writeFull(userFile)
        }.value.toStringId().toProjectId()
    }
}
