package streetlight.server.db.services

import kabinet.console.globalConsole
import klutch.db.DbService
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

private val console = globalConsole.getHandle(SongPlayTableService::class)

class SongPlayTableService(val app: ServerProvider = RuntimeProvider): DbService()
