package streetlight.server.db.services

import klutch.db.DbService
import klutch.db.read
import klutch.db.readAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import streetlight.model.data.TransitRoute
import streetlight.model.data.TransitRouteId
import streetlight.server.db.tables.TransitRouteTable
import streetlight.server.db.tables.toTransitRoute
import streetlight.server.db.tables.writeFull
import streetlight.server.db.tables.writeUpdate

/**
 * Ahoy! This be the TransitRouteTableDao, managin' our charts and routes.
 */
class TransitRouteTableDao : DbService() {

    suspend fun readRoute(transitRouteId: TransitRouteId) = dbQuery {
        TransitRouteTable.read { it.id.eq(transitRouteId.value) }.firstOrNull()?.toTransitRoute()
    }

    suspend fun readAllRoutes() = dbQuery {
        TransitRouteTable.readAll().map { it.toTransitRoute() }
    }

    suspend fun readRoutes(routeIds: Set<String>) = dbQuery {
        TransitRouteTable.read { it.id.inList(routeIds) }.map { it.toTransitRoute() }
    }

    suspend fun createRoute(transitRoute: TransitRoute): TransitRouteId = dbQuery {
        TransitRouteTable.insertAndGetId {
            it.writeFull(transitRoute)
        }.value.let { TransitRouteId(it) }
    }

    suspend fun updateRoute(transitRoute: TransitRoute) = dbQuery {
        TransitRouteTable.update(where = { TransitRouteTable.id.eq(transitRoute.transitRouteId.value) }) {
            it.writeUpdate(transitRoute)
        } == 1
    }

    suspend fun upsert(transitRoute: TransitRoute) = dbQuery {
        TransitRouteTable.upsert(TransitRouteTable.id) {
            it.writeFull(transitRoute)
        }
    }
}
