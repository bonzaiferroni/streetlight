package streetlight.server.db.tables

import klutch.db.tables.UserTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import streetlight.model.core.Event
import streetlight.model.enums.EventStatus

internal object EventTable : IntIdTable() {
    val user = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val location = reference("location_id", LocationTable, onDelete = ReferenceOption.CASCADE)
    val currentRequest = reference("current_song_id", RequestTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val timeStart = long("time_start")
    val hours = float("hours").nullable()
    val url = text("url").nullable()
    val imageUrl = text("image_url").nullable()
    val streamUrl = text("stream_url").nullable()
    val name = text("name").nullable()
    val description = text("description").nullable()
    val status = enumerationByName("status", 20, EventStatus::class)
    val cashTips = float("cash_tips").nullable()
    val cardTips = float("card_tips").nullable()
}

internal fun ResultRow.toEvent() = Event(
    id = this[EventTable.id].value,
    userId = this[EventTable.user].value,
    locationId = this[EventTable.location].value,
    currentRequestId = this[EventTable.currentRequest]?.value,
    timeStart = this[EventTable.timeStart],
    hours = this[EventTable.hours],
    url = this[EventTable.url],
    imageUrl = this[EventTable.imageUrl],
    streamUrl = this[EventTable.streamUrl],
    name = this[EventTable.name],
    description = this[EventTable.description],
    status = this[EventTable.status],
    cashTips = this[EventTable.cashTips],
    cardTips = this[EventTable.cardTips]
)