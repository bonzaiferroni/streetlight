package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.upsert
import streetlight.model.data.TransitStop
import streetlight.model.data.TransitStopId
import streetlight.server.db.tables.TransitStopTable
import streetlight.server.db.tables.toTransitStop
import streetlight.server.db.tables.writeFull

class TransitStopTableDao : DbService() {

    suspend fun readTransitStop(transitStopId: TransitStopId) = dbQuery {
        TransitStopTable.read { it.id.eq(transitStopId.value) }.firstOrNull()?.toTransitStop()
    }

    suspend fun createTransitStop(transitStop: TransitStop) = dbQuery {
        TransitStopTable.insertAndGetId {
            it.writeFull(transitStop)
        }
    }

    suspend fun upsert(transitStop: TransitStop) = dbQuery {
        TransitStopTable.upsert(TransitStopTable.id) {
            it.writeFull(transitStop)
        }
    }
}
