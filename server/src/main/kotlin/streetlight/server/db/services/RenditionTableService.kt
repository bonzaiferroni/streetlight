package streetlight.server.db.services

import kabinet.console.globalConsole
import klutch.db.DbService
import streetlight.server.RuntimeProvider
import streetlight.server.ServerProvider

private val console = globalConsole.getHandle(RenditionTableService::class)

class RenditionTableService(val app: ServerProvider = RuntimeProvider): DbService()
