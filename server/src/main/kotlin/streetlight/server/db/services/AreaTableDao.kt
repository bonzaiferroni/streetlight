package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.readAll
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.AreaId
import streetlight.model.data.NewArea
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.AreaTable
import streetlight.server.db.tables.toArea

class AreaTableDao: DbService() {
    suspend fun readAreas() = dbQuery {
        AreaTable.readAll().map { it.toArea() }
    }

    suspend fun createArea(newArea: NewArea): AreaId = dbQuery {
        AreaTable.insertAndGetId {
            it[this.name] = newArea.name
        }.value.toStringId().toProjectId()
    }
}