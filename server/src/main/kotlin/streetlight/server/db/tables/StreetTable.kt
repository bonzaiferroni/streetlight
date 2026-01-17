package streetlight.server.db.tables

import kabinet.model.GeoPoint
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Street
import streetlight.server.utils.toProjectId

object StreetTable : UUIDTable("street") {
    val name = text("name")
    val points = jsonb<List<GeoPoint>>("points", tableJsonDefault)
}

object StreetTransitRouteTable : Table("street_transit_route") {

}

fun ResultRow.toArea() = Street(
    streetId = toProjectId(StreetTable.id),
    name = this[StreetTable.name],
    points = this[StreetTable.points],
)

// Updaters
fun UpdateBuilder<*>.writeFull(street: Street) {
    this[StreetTable.id] = street.streetId.toUUID()
    writeUpdate(street)
}

fun UpdateBuilder<*>.writeUpdate(street: Street) {
    this[StreetTable.name] = street.name
    this[StreetTable.points] = street.points
}

