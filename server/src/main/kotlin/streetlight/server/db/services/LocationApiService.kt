package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.toPGpoint
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import streetlight.model.data.Location
import streetlight.model.data.NewLocation
import streetlight.model.data.ResourceType
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.toLocation

class LocationApiService: DbService() {

    suspend fun readLocation(locationId: Int) = dbQuery {
        LocationTable.read { it.id.eq(locationId) }
            .firstOrNull()?.toLocation()
    }

    suspend fun readLocations(areaId: Int) = dbQuery {
        LocationTable.read { it.areaId.eq(areaId) }
            .map { it.toLocation() }
    }

    suspend fun createLocation(newLocation: NewLocation) = dbQuery {
        LocationTable.insertAndGetId {
            it[this.areaId] = newLocation.areaId
            it[this.name] = newLocation.name
            it[this.geoPoint] = newLocation.geoPoint.toPGpoint()
            it[this.resources] = emptyList()
        }.value
    }

    suspend fun updateLocation(userId: Long, location: Location) = dbQuery {
        LocationTable.update(where = { LocationTable.id.eq(location.id) and LocationTable.userId.eq(userId)}) {
            it[this.name] = location.name
            it[this.description] = location.description
            it[this.address] = location.address
            it[this.geoPoint] = location.geoPoint.toPGpoint()
        } == 1
    }
}