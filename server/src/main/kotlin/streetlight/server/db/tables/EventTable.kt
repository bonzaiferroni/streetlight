package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.tables.UserTable
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Event
import streetlight.model.data.EventStatus
import streetlight.model.data.EventType
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toProjectIdOrNull
import streetlight.server.utils.toUserId

object EventTable : UUIDTable("event") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val locationId = reference("location_id", LocationTable, onDelete = ReferenceOption.CASCADE)
    val currentRequest = reference("current_song_id", RequestTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val url = text("url").nullable()
    val imageUrl = text("image_url").nullable()
    val streamUrl = text("stream_url").nullable()
    val name = text("name")
    val description = text("description").nullable()
    val status = enumeration<EventStatus>("status")
    val eventType = enumeration<EventType>("type").default(EventType.StreetPerformance)
    val cashTips = float("cash_tips").nullable()
    val cardTips = float("card_tips").nullable()
    val startsAt = datetime("starts_at")
    val endsAt = datetime("ends_at")
    val updatedAt = datetime("updated_at")
    val createdAt = datetime("created_at")
}

fun ResultRow.toEvent() = Event(
    eventId = toProjectId(EventTable.id),
    userId = toUserId(EventTable.userId),
    locationId = toProjectId(EventTable.locationId),
    currentRequestId = toProjectIdOrNull(EventTable.currentRequest),
    url = this[EventTable.url],
    imageUrl = this[EventTable.imageUrl],
    streamUrl = this[EventTable.streamUrl],
    title = this[EventTable.name],
    description = this[EventTable.description],
    status = this[EventTable.status],
    type = this[EventTable.eventType],
    cashTips = this[EventTable.cashTips],
    cardTips = this[EventTable.cardTips],
    startsAt = this[EventTable.startsAt].toInstantFromUtc(),
    endsAt = this[EventTable.endsAt].toInstantFromUtc(),
    updatedAt = this[EventTable.updatedAt].toInstantFromUtc(),
    createdAt = this[EventTable.createdAt].toInstantFromUtc()
)

fun UpdateBuilder<*>.writeFull(event: Event) {
    this[EventTable.id] = event.eventId.toUUID()
    this[EventTable.userId] = event.userId.toUUID()
    this[EventTable.locationId] = event.locationId.toUUID()
    this[EventTable.currentRequest] = event.currentRequestId?.toUUID()
    this[EventTable.url] = event.url
    this[EventTable.createdAt] = event.createdAt.toLocalDateTimeUtc()
    writeUpdate(event)
}

fun UpdateBuilder<*>.writeUpdate(event: Event) {
    this[EventTable.imageUrl] = event.imageUrl
    this[EventTable.streamUrl] = event.streamUrl
    this[EventTable.name] = event.title
    this[EventTable.description] = event.description
    this[EventTable.status] = event.status
    this[EventTable.eventType] = event.type
    this[EventTable.cashTips] = event.cashTips
    this[EventTable.cardTips] = event.cardTips
    this[EventTable.startsAt] = event.startsAt.toLocalDateTimeUtc()
    this[EventTable.endsAt] = event.endsAt.toLocalDateTimeUtc()
    this[EventTable.updatedAt] = event.updatedAt.toLocalDateTimeUtc()
}