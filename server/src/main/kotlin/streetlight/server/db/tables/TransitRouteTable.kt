package streetlight.server.db.tables

import kampfire.model.GeoPoint
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.TransitRoute
import streetlight.model.data.TransitRouteId
import streetlight.model.data.VehicleType

/**
 * Ahoy! This be the TransitRouteTable, where we store the charts for our noble vessels.
 */
object TransitRouteTable: IdTable<String>("transit_route") {
    override val id = text("id").entityId()
    val shortName = text("short_name")
    val longName = text("long_name")
    val description = text("description").nullable()
    val vehicleType = enumeration<VehicleType>("vehicle_type").nullable()
    val points = json<Array<GeoPoint>>("points", Json.Default)

    override val primaryKey = PrimaryKey(id)
}

object TransitRouteStopTable: Table("transit_route_stop") {
    val transitRouteId = reference("transit_route_id", TransitRouteTable.id, onDelete = ReferenceOption.CASCADE)
    val transitStopId = reference("transit_stop_id", TransitStopTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(transitRouteId, transitStopId)
}

/**
 * Map a row from the sea of data into a TransitRoute.
 */
fun ResultRow.toTransitRoute() = TransitRoute(
    transitRouteId = TransitRouteId(this[TransitRouteTable.id].value),
    shortName = this[TransitRouteTable.shortName],
    longName = this[TransitRouteTable.longName],
    description = this[TransitRouteTable.description],
    vehicleType = this[TransitRouteTable.vehicleType],
    points = this[TransitRouteTable.points].toList(),
)

/**
 * Write the full cargo of a TransitRoute to the table.
 */
fun UpdateBuilder<*>.writeFull(transitRoute: TransitRoute) {
    this[TransitRouteTable.id] = transitRoute.transitRouteId.value
    writeUpdate(transitRoute)
}

/**
 * Update the changeable parts of our TransitRoute cargo.
 */
fun UpdateBuilder<*>.writeUpdate(transitRoute: TransitRoute) {
    this[TransitRouteTable.shortName] = transitRoute.shortName
    this[TransitRouteTable.longName] = transitRoute.longName
    this[TransitRouteTable.description] = transitRoute.description
    this[TransitRouteTable.vehicleType] = transitRoute.vehicleType
    this[TransitRouteTable.points] = transitRoute.points.toTypedArray()
}
