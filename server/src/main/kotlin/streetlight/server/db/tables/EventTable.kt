package streetlight.server.db.tables

import kabinet.model.UserId
import kabinet.utils.toInstantFromUtc
import klutch.db.tables.UserTable
import klutch.utils.toStringId
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import streetlight.model.data.Event
import streetlight.model.data.EventId
import streetlight.model.data.EventStatus
import streetlight.model.data.LocationId
import streetlight.model.data.RequestId
import kotlin.time.Duration.Companion.hours

internal object EventTable : UUIDTable("event") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val locationId = reference("location_id", LocationTable, onDelete = ReferenceOption.CASCADE)
    val currentRequest = reference("current_song_id", RequestTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val url = text("url").nullable()
    val imageUrl = text("image_url").nullable()
    val streamUrl = text("stream_url").nullable()
    val name = text("name").nullable()
    val description = text("description").nullable()
    val status = enumeration<EventStatus>("status")
    val cashTips = float("cash_tips").nullable()
    val cardTips = float("card_tips").nullable()
    val durationHours = float("duration_hours").nullable()
    val startsAt = datetime("starts_at")
}

internal fun ResultRow.toEvent() = Event(
    eventId = EventId(this[EventTable.id].value.toStringId()),
    userId = UserId(this[EventTable.userId].value.toStringId()),
    locationId = LocationId(this[EventTable.locationId].value.toStringId()),
    currentRequestId = this[EventTable.currentRequest]?.value?.let { RequestId(it.toStringId()) },
    url = this[EventTable.url],
    imageUrl = this[EventTable.imageUrl],
    streamUrl = this[EventTable.streamUrl],
    name = this[EventTable.name],
    description = this[EventTable.description],
    status = this[EventTable.status],
    cashTips = this[EventTable.cashTips],
    cardTips = this[EventTable.cardTips],
    hours = this[EventTable.durationHours]?.toDouble()?.hours,
    startsAt = this[EventTable.startsAt].toInstantFromUtc(),
)