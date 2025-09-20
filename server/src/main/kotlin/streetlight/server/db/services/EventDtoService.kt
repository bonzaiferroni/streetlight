package streetlight.server.db.services

import kabinet.model.UserId
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.read
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent
import streetlight.model.data.EventStatus
import streetlight.model.data.LocationId
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.toEvent

class EventDtoService: DbService() {
    suspend fun readActiveEvents() = dbQuery {
        EventTable.read { it.status.neq(EventStatus.Finished) }
            .map { it.toEvent() }
    }

    suspend fun createEvent(userId: UserId, newEvent: NewEvent): EventId = dbQuery {
        EventTable.insertAndGetId {
            it[this.userId] = userId.toUUID()
            it[this.locationId] = newEvent.locationId.toUUID()
            it[this.startsAt] = newEvent.startsAt.toLocalDateTimeUtc()
            it[this.status] = EventStatus.Pending
        }.value.toStringId().toProjectId()
    }
}