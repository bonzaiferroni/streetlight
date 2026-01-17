package streetlight.server.db.tables

import klutch.db.tables.RefreshTokenTable.double
import klutch.db.tables.RefreshTokenTable.nullable
import klutch.db.tables.RefreshTokenTable.text
import klutch.utils.*
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.TransitStop
import streetlight.model.data.TransitStopId
import streetlight.server.utils.toProjectId

object TransitStopTable : IdTable<String>("transit_stop") {
    override val id = text("id").entityId()
    val name = text("name")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toTransitStop() = TransitStop(
    transitStopId = TransitStopId(this[TransitStopTable.id].value),
    name = this[TransitStopTable.name],
    latitude = this[TransitStopTable.latitude],
    longitude = this[TransitStopTable.longitude],
    description = this[TransitStopTable.description],
)

fun UpdateBuilder<*>.writeFull(transitStop: TransitStop) {
    this[TransitStopTable.id] = transitStop.transitStopId.value
    writeUpdate(transitStop)
}

fun UpdateBuilder<*>.writeUpdate(transitStop: TransitStop) {
    this[TransitStopTable.name] = transitStop.name
    this[TransitStopTable.latitude] = transitStop.latitude
    this[TransitStopTable.longitude] = transitStop.longitude
    this[TransitStopTable.description] = transitStop.description
}
