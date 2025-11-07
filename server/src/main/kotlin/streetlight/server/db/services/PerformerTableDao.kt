package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import streetlight.model.data.Performer
import streetlight.model.data.PerformerId
import streetlight.server.db.tables.PerformerTable
import streetlight.server.db.tables.toPerformer
import streetlight.server.db.tables.writeFull

class PerformerTableDao: DbService() {

    suspend fun readById(performerId: PerformerId): Performer? = dbQuery {
        PerformerTable.read { it.id.eq(performerId) }.firstOrNull()?.toPerformer()
    }

    suspend fun create(performer: Performer) = dbQuery {
        PerformerTable.insert { it.writeFull(performer) }
    }
}
