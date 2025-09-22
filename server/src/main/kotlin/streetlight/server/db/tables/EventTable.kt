package streetlight.server.db.tables

import kabinet.model.UserId
import kabinet.utils.toHours
import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
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
    val name = text("name")
    val description = text("description").nullable()
    val status = enumeration<EventStatus>("status")
    val cashTips = float("cash_tips").nullable()
    val cardTips = float("card_tips").nullable()
    val durationHours = float("duration_hours").nullable()
    val startsAt = datetime("starts_at")
    val createdAt = datetime("created_at")
}

internal fun ResultRow.toEvent() = Event(
    eventId = EventId(this[EventTable.id].value.toStringId()),
    userId = UserId(this[EventTable.userId].value.toStringId()),
    locationId = LocationId(this[EventTable.locationId].value.toStringId()),
    currentRequestId = this[EventTable.currentRequest]?.value?.let { RequestId(it.toStringId()) },
    url = this[EventTable.url],
    imageUrl = this[EventTable.imageUrl],
    streamUrl = this[EventTable.streamUrl],
    title = this[EventTable.name],
    description = this[EventTable.description],
    status = this[EventTable.status],
    cashTips = this[EventTable.cashTips],
    cardTips = this[EventTable.cardTips],
    hours = this[EventTable.durationHours]?.toDouble()?.hours,
    startsAt = this[EventTable.startsAt].toInstantFromUtc(),
    createdAt = this[EventTable.createdAt].toInstantFromUtc()
)

internal fun UpdateBuilder<*>.writeFull(event: Event) {
    this[EventTable.id] = event.eventId.toUUID()
    this[EventTable.userId] = event.userId.toUUID()
    this[EventTable.locationId] = event.locationId.toUUID()
    this[EventTable.currentRequest] = event.currentRequestId?.toUUID()
    this[EventTable.url] = event.url
    this[EventTable.createdAt] = event.createdAt.toLocalDateTimeUtc()
    writeUpdate(event)
}

internal fun UpdateBuilder<*>.writeUpdate(event: Event) {
    this[EventTable.imageUrl] = event.imageUrl
    this[EventTable.streamUrl] = event.streamUrl
    this[EventTable.name] = event.title
    this[EventTable.description] = event.description
    this[EventTable.status] = event.status
    this[EventTable.cashTips] = event.cashTips
    this[EventTable.cardTips] = event.cardTips
    this[EventTable.durationHours] = event.hours?.toHours()
    this[EventTable.startsAt] = event.startsAt.toLocalDateTimeUtc()
}