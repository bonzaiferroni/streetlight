package streetlight.server.db.tables

import klutch.db.tables.UserTable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.json.json
import streetlight.model.core.GeoPoint
import streetlight.model.core.Location
import streetlight.model.enums.ResourceType

internal object LocationTable : IntIdTable() {
    val userId = reference("user_id", UserTable, ReferenceOption.SET_NULL).nullable()
    val areaId = reference("area_id", AreaTable, ReferenceOption.SET_NULL).nullable()
    val name = text("name").nullable()
    val description = text("description").nullable()
    val address = text("address").nullable()
    val notes = text("notes").nullable()
    val latitude = double("latitude") // todo: use GeoPoint
    val longitude = double("longitude")
    val resources = json<Array<ResourceType>>("type", Json)
}

internal fun ResultRow.toLocation() = Location(
    id = this[LocationTable.id].value,
    userId = this[LocationTable.userId]?.value,
    areaId = this[LocationTable.areaId]?.value,
    name = this[LocationTable.name],
    description = this[LocationTable.description],
    address = this[LocationTable.address],
    notes = this[LocationTable.notes],
    geoPoint = GeoPoint(this[LocationTable.latitude], this[LocationTable.longitude]),
    resources = this[LocationTable.resources].toSet()
)