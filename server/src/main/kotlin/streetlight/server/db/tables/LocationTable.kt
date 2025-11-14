package streetlight.server.db.tables

import kabinet.utils.toInstantFromUtc
import kabinet.utils.toLocalDateTimeUtc
import klutch.db.defaultNow
import klutch.db.tables.UserTable
import klutch.utils.*
import klutch.db.point
import klutch.db.readColumn
import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.ResourceType
import streetlight.server.utils.toProjectId
import streetlight.server.utils.toProjectIdOrNull
import streetlight.server.utils.toUserIdOrNull

object LocationTable : UUIDTable("location") {
    val userId = reference("user_id", UserTable, ReferenceOption.SET_NULL).nullable()
    val areaId = reference("area_id", StreetTable, ReferenceOption.SET_NULL).nullable()
    val name = text("name")
    val description = text("description").nullable()
    val address = text("address").nullable()
    val notes = text("notes").nullable()
    val geoPoint = point("geo_point")
    val resources = array<Int>("resources")
    val updatedAt = datetime("updated_at").defaultNow()
    val createdAt = datetime("created_at").defaultNow()
}

fun ResultRow.toLocation() = Location(
    locationId = toProjectId(LocationTable.id),
    userId = toUserIdOrNull(LocationTable.userId),
    streetId = toProjectIdOrNull(LocationTable.areaId),
    name = this[LocationTable.name],
    description = this[LocationTable.description],
    address = this[LocationTable.address],
    notes = this[LocationTable.notes],
    geoPoint = this[LocationTable.geoPoint].toGeoPoint(),
    resources = this[LocationTable.resources].map { ResourceType.entries[it] }.toSet(),
    updatedAt = this[LocationTable.updatedAt].toInstantFromUtc(),
    createdAt = this[LocationTable.createdAt].toInstantFromUtc(),
)

// Updaters
fun UpdateBuilder<*>.writeFull(location: Location) {
    this[LocationTable.id] = location.locationId.toUUID()
    this[LocationTable.userId] = location.userId?.toUUID()
    this[LocationTable.areaId] = location.streetId?.toUUID()
    this[LocationTable.createdAt] = location.createdAt.toLocalDateTimeUtc()
    writeUpdate(location)
}

fun UpdateBuilder<*>.writeUpdate(location: Location) {
    this[LocationTable.name] = location.name
    this[LocationTable.description] = location.description
    this[LocationTable.address] = location.address
    this[LocationTable.notes] = location.notes
    this[LocationTable.geoPoint] = location.geoPoint.toPGpoint()
    this[LocationTable.resources] = location.resources.map { it.ordinal }
    this[LocationTable.updatedAt] = location.updatedAt.toLocalDateTimeUtc()
}

// Property helpers
fun LocationTable.readName(locationId: LocationId) = readColumn(name) { id.eq(locationId) }.singleOrNull()