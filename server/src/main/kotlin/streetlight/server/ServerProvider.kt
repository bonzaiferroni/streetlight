package streetlight.server

import kabinet.utils.Environment
import streetlight.server.db.services.AreaTableDao
import streetlight.server.db.services.EventTableDao
import streetlight.server.db.services.LocationTableDao
import streetlight.server.db.services.SongTableDao
import streetlight.server.db.services.RenditionTableDao
import streetlight.server.db.services.SongTableService

interface ServerProvider {
    val env: Environment
    val dao: ServerDao
    val service: ServerService
}

class ServerDao(
    val location: LocationTableDao = LocationTableDao(),
    val area: AreaTableDao = AreaTableDao(),
    val song: SongTableDao = SongTableDao(),
    val event: EventTableDao = EventTableDao(),
    val songPlay: RenditionTableDao = RenditionTableDao(),
)

class ServerService(
    val song: SongTableService = SongTableService()
)