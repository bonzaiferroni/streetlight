package streetlight.server.db.tables

import klutch.db.tables.UserTable
import klutch.utils.*
import klutch.db.point
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.json.json
import streetlight.model.data.Location
import streetlight.model.data.ResourceType

internal object LocationTable : IntIdTable() {
    val userId = reference("user_id", UserTable, ReferenceOption.SET_NULL).nullable()
    val areaId = reference("area_id", AreaTable, ReferenceOption.SET_NULL).nullable()
    val name = text("name").nullable()
    val description = text("description").nullable()
    val address = text("address").nullable()
    val notes = text("notes").nullable()
    val geoPoint = point("geo_point")
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
    geoPoint = this[LocationTable.geoPoint].toGeoPoint(),
    resources = this[LocationTable.resources].toSet()
)