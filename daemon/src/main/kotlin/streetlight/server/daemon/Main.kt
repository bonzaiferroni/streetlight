package streetlight.server.daemon

import kabinet.utils.Environment
import klutch.server.KoinProvider
import klutch.server.provide
import kampfire.model.toDataOr
import kotlinx.coroutines.runBlocking
import org.koin.dsl.koinApplication
import streetlight.model.data.EventSubtype
import streetlight.model.data.EventType
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.KoogEmbeddingsClient
import streetlight.server.daemon.agent.getClassifierLabel
import streetlight.server.daemon.agent.KoogHtmlParserClient
import streetlight.server.daemon.agent.readLmConfig
import streetlight.server.daemon.crawler.startCrawler
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

    val embeddingsClient = KoogEmbeddingsClient()
    val typeVectors = embeddingsClient.getCachedVectors(EventType.entries) { it.getClassifierLabel() }
        .toDataOr { error("Unable to embed event types: ${it.message}") }
    val subtypeVectors = embeddingsClient.getCachedVectors(EventSubtype.entries) { it.getClassifierLabel() }
        .toDataOr { error("Unable to embed event subtypes: ${it.message}") }
    val classifier = EntityClassifier(embeddingsClient, typeVectors, subtypeVectors)

    // startCrawler(server, KoogHtmlParserClient(env.readLmConfig()), classifier)
}