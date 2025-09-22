package streetlight.server.db.services

import kabinet.model.UserId
import klutch.db.DbService
import klutch.db.read
import klutch.utils.eq
import klutch.utils.toStringId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.update
import streetlight.model.data.AreaId
import streetlight.model.data.Location
import streetlight.model.data.LocationId
import streetlight.model.data.NewLocation
import streetlight.model.data.toProjectId
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.toLocation
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate

class LocationTableDao: DbService() {

    suspend fun readLocation(locationId: LocationId) = dbQuery {
        LocationTable.read { it.id.eq(locationId) }.firstOrNull()?.toLocation()
    }

    suspend fun readLocations(areaId: AreaId) = dbQuery {
        LocationTable.read { it.areaId.eq(areaId) }.map { it.toLocation() }
    }

    suspend fun createLocation(userId: UserId, newLocation: NewLocation): LocationId = dbQuery {
        LocationTable.insertAndGetId {
            it.writeFull(Location(
                locationId = LocationId.random(),
                userId = userId,
                areaId = newLocation.areaId,
                name = newLocation.name,
                description = null,
                address = null,
                notes = null,
                geoPoint = newLocation.geoPoint,
                resources = emptySet()
            ))
        }.value.toStringId().toProjectId()
    }

    suspend fun updateLocation(userId: UserId, location: Location) = dbQuery {
        LocationTable.update(where = { LocationTable.id.eq(location.locationId) and LocationTable.userId.eq(userId)}) {
            it.writeUpdate(location)
        } == 1
    }

    suspend fun searchLocations(query: String) = dbQuery {
        val query = query.lowercase()
        LocationTable.read { it.name.lowerCase().like("%$query%") or it.description.lowerCase().like("%$query%") }.map { it.toLocation() }
    }
}