package streetlight.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.toInstantUtc
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import streetlight.model.data.Event
import streetlight.model.data.EventStatus
import kotlin.time.Duration.Companion.hours

internal object EventTable : LongIdTable("event") {
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
    id = this[EventTable.id].value,
    userId = this[EventTable.userId].value,
    locationId = this[EventTable.locationId].value,
    currentRequestId = this[EventTable.currentRequest]?.value,
    url = this[EventTable.url],
    imageUrl = this[EventTable.imageUrl],
    streamUrl = this[EventTable.streamUrl],
    name = this[EventTable.name],
    description = this[EventTable.description],
    status = this[EventTable.status],
    cashTips = this[EventTable.cashTips],
    cardTips = this[EventTable.cardTips],
    hours = this[EventTable.durationHours]?.toDouble()?.hours,
    startsAt = this[EventTable.startsAt].toInstantUtc(),
)