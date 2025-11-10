package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.readAll
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.StreetId
import streetlight.model.data.NewStreet
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.StreetTable
import streetlight.server.db.tables.toArea

class StreetTableDao: DbService() {
    suspend fun readAreas() = dbQuery {
        StreetTable.readAll().map { it.toArea() }
    }

    suspend fun create(newStreet: NewStreet): StreetId = dbQuery {
        StreetTable.insertAndGetId {
            it[this.name] = newStreet.name
        }.value.toStringId().toProjectId()
    }
}