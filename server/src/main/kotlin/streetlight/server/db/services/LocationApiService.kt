package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.utils.toPGpoint
import org.jetbrains.exposed.sql.insertAndGetId
import streetlight.model.data.NewLocation
import streetlight.model.data.ResourceType
import streetlight.server.db.tables.LocationTable
import streetlight.server.db.tables.toLocation

class LocationApiService: DbService() {
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
}