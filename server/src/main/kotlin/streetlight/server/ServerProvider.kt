package streetlight.server

import kabinet.utils.Environment
import klutch.db.services.UserTableDao
import klutch.db.services.UserTableService
import streetlight.server.db.services.AreaTableDao
import streetlight.server.db.services.EventTableDao
import streetlight.server.db.services.LocationTableDao
import streetlight.server.db.services.SongTableDao
import streetlight.server.db.services.RenditionTableDao
import streetlight.server.db.services.SongTableService
import streetlight.server.db.services.SparkTableDao
import streetlight.server.db.services.RequestTableDao

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
    val user: UserTableDao = UserTableDao(),
    val spark: SparkTableDao = SparkTableDao(),
    val request: RequestTableDao = RequestTableDao(),
)

class ServerService(
    val song: SongTableService = SongTableService(),
    val service: UserTableService = UserTableService(),
)