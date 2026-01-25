package streetlight.server.db.tables

import kampfire.model.GeoPoint
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Area
import streetlight.model.data.AreaType
import streetlight.server.utils.toProjectId

object AreaTable : UUIDTable("area") {
    val name = text("name")
    val points = jsonb<List<GeoPoint>>("points", tableJsonDefault)
    val areaType = enumeration<AreaType>("area_type")
}

object AreaTransitRouteTable : Table("area_transit_route") {

}

fun ResultRow.toArea() = Area(
    areaId = toProjectId(AreaTable.id),
    name = this[AreaTable.name],
    points = this[AreaTable.points],
    areaType = this[AreaTable.areaType],
)

// Updaters
fun UpdateBuilder<*>.writeFull(area: Area) {
    this[AreaTable.id] = area.areaId.toUUID()
    writeUpdate(area)
}

fun UpdateBuilder<*>.writeUpdate(area: Area) {
    this[AreaTable.name] = area.name
    this[AreaTable.points] = area.points
    this[AreaTable.areaType] = area.areaType
}

