package streetlight.server.db.tables

import kabinet.model.UserId
import klutch.db.tables.UserTable
import klutch.utils.*
import klutch.db.point
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
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
    locationId = LocationId(this[LocationTable.id].value.toStringId()),
    userId = this[LocationTable.userId]?.value?.let { UserId(it.toStringId()) },
    areaId = this[LocationTable.areaId]?.value?.let { AreaId(it.toStringId()) },
    name = this[LocationTable.name],
    description = this[LocationTable.description],
    address = this[LocationTable.address],
    notes = this[LocationTable.notes],
    geoPoint = this[LocationTable.geoPoint].toGeoPoint(),
    resources = this[LocationTable.resources].map { ResourceType.entries[it] }.toSet()
)