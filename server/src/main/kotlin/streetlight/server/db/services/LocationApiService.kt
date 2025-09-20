package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toPGpoint
import klutch.utils.toStringId
import klutch.utils.toUUID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import streetlight.model.data.AreaId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewLocation
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.toLocation

class LocationApiService: DbService() {

    suspend fun readLocation(locationId: LocationId) = dbQuery {
        LocationTable.read { it.id.eq(locationId) }
            .firstOrNull()?.toLocation()
    }

    suspend fun readLocations(areaId: AreaId) = dbQuery {
        LocationTable.read { it.areaId.eq(areaId) }
            .map { it.toLocation() }
    }

    suspend fun createLocation(newLocation: NewLocation): LocationId = dbQuery {
        LocationTable.insertAndGetId {
            it[this.areaId] = newLocation.areaId.toUUID()
            it[this.name] = newLocation.name
            it[this.geoPoint] = newLocation.geoPoint.toPGpoint()
            it[this.resources] = emptyList()
        }.value.toStringId().toProjectId()
    }

    suspend fun updateLocation(userId: UserId, location: Location) = dbQuery {
        LocationTable.update(where = { LocationTable.id.eq(location.locationId) and LocationTable.userId.eq(userId)}) {
            it[this.name] = location.name
            it[this.description] = location.description
            it[this.address] = location.address
            it[this.geoPoint] = location.geoPoint.toPGpoint()
        } == 1
    }
}