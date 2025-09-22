package streetlight.server.db.tables

import kabinet.model.UserId
import klutch.db.tables.UserTable
import klutch.utils.*
import klutch.db.point
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.AreaId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.ResourceType

internal object LocationTable : UUIDTable("location") {
    val userId = reference("user_id", UserTable, ReferenceOption.SET_NULL).nullable()
    val areaId = reference("area_id", AreaTable, ReferenceOption.SET_NULL).nullable()
    val name = text("name").nullable()
    val description = text("description").nullable()
    val address = text("address").nullable()
    val notes = text("notes").nullable()
    val geoPoint = point("geo_point")
    val resources = array<Int>("resources")
}

internal fun ResultRow.toLocation() = Location(
    locationId = LocationId(this[table.id].value.toStringId()),
    userId = this[table.userId]?.value?.let { UserId(it.toStringId()) },
    areaId = this[table.areaId]?.value?.let { AreaId(it.toStringId()) },
    name = this[table.name],
    description = this[table.description],
    address = this[table.address],
    notes = this[table.notes],
    geoPoint = this[table.geoPoint].toGeoPoint(),
    resources = this[table.resources].map { ResourceType.entries[it] }.toSet()
)

internal fun UpdateBuilder<*>.writeFull(location: Location) {
    this[table.id] = location.locationId.toUUID()
    this[table.userId] = location.userId?.toUUID()
    this[table.areaId] = location.areaId?.toUUID()
    writeUpdate(location)
}

internal fun UpdateBuilder<*>.writeUpdate(location: Location) {
    this[table.name] = location.name
    this[table.description] = location.description
    this[table.address] = location.address
    this[table.notes] = location.notes
    this[table.geoPoint] = location.geoPoint.toPGpoint()
    this[table.resources] = location.resources.map { it.ordinal }
}

private val table = LocationTable