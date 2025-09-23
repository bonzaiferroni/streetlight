package streetlight.server

import kabinet.utils.Environment
import klutch.environment.readEnvFromPath
import streetlight.server.db.services.AreaTableDao
import streetlight.server.db.services.EventTableDao
import streetlight.server.db.services.LocationTableDao
import streetlight.server.db.services.SongTableDao
import streetlight.server.db.services.SongPlayTableDao

interface ServerProvider {
    val env: Environment
    val dao: ServerDao
}

class ServerDao(
    val location: LocationTableDao = LocationTableDao(),
    val area: AreaTableDao = AreaTableDao(),
    val song: SongTableDao = SongTableDao(),
    val event: EventTableDao = EventTableDao(),
    val songPlay: SongPlayTableDao = SongPlayTableDao(),
)