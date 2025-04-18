package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.toLocalDateTimeUtc
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.NewEvent
import streetlight.model.data.EventStatus
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.toEvent

class EventDtoService: DbService() {
    suspend fun readActiveEvents() = dbQuery {
        EventTable.read { it.status.neq(EventStatus.Finished) }
            .map { it.toEvent() }
    }

    suspend fun createEvent(userId: Long, newEvent: NewEvent) = dbQuery {
        EventTable.insertAndGetId {
            it[this.userId] = userId
            it[this.locationId] = newEvent.locationId
            it[this.startsAt] = newEvent.startsAt.toLocalDateTimeUtc()
            it[this.status] = EventStatus.Pending
        }.value
    }
}