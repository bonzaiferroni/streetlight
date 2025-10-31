package streetlight.server.db.services

import kabinet.model.UserId
import kabinet.utils.toDayDescription
import klutch.db.DbService
import klutch.db.deleteSingle
import klutch.db.read
import klutch.db.updateSingleWhere
import klutch.utils.eq
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.NewEvent
import streetlight.model.data.EventStatus
import streetlight.server.db.tables.EventTable
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.readName
import streetlight.server.db.tables.toEvent
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate
import kotlin.time.Duration.Companion.hours

class EventTableDao: DbService() {
    suspend fun readActiveEvents() = dbQuery {
        EventTable.read { it.status.neq(EventStatus.Finished) }
            .map { it.toEvent() }
    }

    suspend fun readEvent(eventId: EventId) = dbQuery {
        EventTable.read { it.id.eq(eventId) }.firstOrNull()?.toEvent()
    }

    suspend fun createEvent(userId: UserId, event: NewEvent) = dbQuery {
        val now = Clock.System.now()
        Event(
            eventId = EventId.random(),
            userId = userId,
            locationId = event.locationId,
            currentRequestId = null,
            url = null,
            imageUrl = null,
            streamUrl = null,
            title = event.title,
            description = null,
            status = EventStatus.Pending,
            eventType = event.eventType,
            cashTips = null,
            cardTips = null,
            startsAt = event.startsAt,
            endsAt = event.startsAt + 1.hours,
            updatedAt = now,
            createdAt = now,
        ).also { event -> EventTable.insert { it.writeFull(event) } }
    }

    suspend fun updateEvent(userId: UserId, event: Event): Boolean = dbQuery {
        EventTable.updateSingleWhere({ EventTable.userId.eq(userId) and EventTable.id.eq(event.eventId) }) {
            it.writeUpdate(event)
        } != null
    }

    suspend fun deleteEvent(userId: UserId, eventId: EventId): Boolean = dbQuery {
        EventTable.deleteSingle { EventTable.userId.eq(userId) and EventTable.id.eq(eventId) }
    }
}