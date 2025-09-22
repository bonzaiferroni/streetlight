package streetlight.server.db.services

import kabinet.model.UserId
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.DbService
import klutch.db.read
import klutch.db.updateSingleWhere
import klutch.utils.eq
import klutch.utils.toStringId
import klutch.utils.toUUID
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent
import streetlight.model.data.EventStatus
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.toEvent
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate

class EventTableDao: DbService() {
    suspend fun readActiveEvents() = dbQuery {
        EventTable.read { it.status.neq(EventStatus.Finished) }
            .map { it.toEvent() }
    }

    suspend fun createEvent(userId: UserId, newEvent: NewEvent) = dbQuery {
        Event(
            eventId = EventId.random(),
            userId = userId,
            locationId = newEvent.locationId,
            currentRequestId = null,
            url = null,
            imageUrl = null,
            streamUrl = null,
            title = null,
            description = null,
            status = EventStatus.Pending,
            cashTips = null,
            cardTips = null,
            hours = null,
            startsAt = newEvent.startsAt,
            createdAt = Clock.System.now(),
        ).also { event -> EventTable.insert { it.writeFull(event) } }
    }

    suspend fun updateEvent(userId: UserId, event: Event): Boolean = dbQuery {
        EventTable.updateSingleWhere({ EventTable.userId.eq(userId) and EventTable.id.eq(event.eventId) }) {
            it.writeUpdate(event)
        } != null
    }
}