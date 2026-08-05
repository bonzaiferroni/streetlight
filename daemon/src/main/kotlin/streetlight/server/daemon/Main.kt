package streetlight.server.daemon

import kabinet.utils.Environment
import klutch.server.KoinProvider
import klutch.server.provide
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import streetlight.server.db.connectDb
import streetlight.server.model.ClientFacade
import streetlight.server.model.DaoFacade
import streetlight.server.model.Server
import streetlight.server.model.serverModule

fun main() = runBlocking {
    println("hello daemon!")
    val koin = koinApplication {
        modules(serverModule)
    }.koin

    val provider = KoinProvider(koin)
    val dao = provider.provide<DaoFacade>()
    val client = provider.provide<ClientFacade>()
    val server = Server(provider, dao, client)
    val env = server.provide<Environment>()

    val db = connectDb(env)

    startParseDaemon(server)
}