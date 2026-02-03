package streetlight.server.db.tables

import kampfire.model.GeoPoint
import klutch.utils.toUUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Community
import streetlight.model.data.CommunityType
import streetlight.server.utils.toProjectId

object AreaTable : UUIDTable("area") {
    val name = text("name")
    val points = jsonb<List<GeoPoint>>("points", tableJsonDefault)
    val communityType = enumeration<CommunityType>("area_type")
}

object AreaTransitRouteTable : Table("area_transit_route") {

}

object AreaLocationTable : Table("area_location_table") {
    val areaId = reference("area_id", AreaTable, ReferenceOption.CASCADE).nullable()
    val locationId = reference("location_id", LocationTable, ReferenceOption.CASCADE).nullable()

    override val primaryKey = PrimaryKey(areaId, locationId)
}

fun ResultRow.toArea() = Community(
    communityId = toProjectId(AreaTable.id),
    name = this[AreaTable.name],
    points = this[AreaTable.points],
    communityType = this[AreaTable.communityType],
)

// Updaters
fun UpdateBuilder<*>.writeFull(community: Community) {
    this[AreaTable.id] = community.communityId.toUUID()
    writeUpdate(community)
}

fun UpdateBuilder<*>.writeUpdate(community: Community) {
    this[AreaTable.name] = community.name
    this[AreaTable.points] = community.points
    this[AreaTable.communityType] = community.communityType
}

