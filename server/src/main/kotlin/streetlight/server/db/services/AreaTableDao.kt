package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.readAll
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.CommunityId
import streetlight.model.data.NewArea
import streetlight.model.data.Community
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.AreaTable
import streetlight.server.db.tables.toArea
import streetlight.server.db.tables.writeFull

class AreaTableDao: DbService() {
    suspend fun readAreas() = dbQuery {
        AreaTable.readAll().map { it.toArea() }
    }

    suspend fun create(newArea: NewArea): CommunityId = dbQuery {
        AreaTable.insertAndGetId {
            it.writeFull(
                Community(
                    communityId = CommunityId.random(),
                    name = newArea.name,
                    points = emptyList(),
                    communityType = newArea.communityType
                )
            )
        }.value.toStringId().toProjectId()
    }
}