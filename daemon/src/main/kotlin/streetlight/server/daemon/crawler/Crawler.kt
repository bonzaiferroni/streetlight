package streetlight.server.daemon.crawler

import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.model.Problem
import klutch.server.provide
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.server.daemon.agent.HtmlParserClient
import streetlight.server.daemon.agent.SchemaMediator
import streetlight.model.data.EventFeed
import streetlight.model.data.Lead
import streetlight.model.data.ParseMode
import streetlight.model.data.toOriginId
import streetlight.server.model.MapReferenceClient
import streetlight.server.model.Server
import streetlight.server.plugins.logger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The reader of each feed that is due, fetching its pages with [fetcher] and asking the LM through [client] for the
 * schemas it needs.
 */
class Crawler(val server: Server, client: HtmlParserClient, val fetcher: PageFetcher) {

    val dao = server.dao
    val mediator = SchemaMediator(client, dao, lmRetryCount)
    val spawner = LocationSpawner(server, server.provide<MapReferenceClient>())
    val log = KotlinLogging.logger(Crawler::class)

    var startedAt = Instant.DISTANT_PAST
        private set

    suspend fun start() {
        while (true) {
            dao.readCheckable(checkInterval - 1.hours).forEach { lead ->
                if (lead is EventFeed && lead.parseMode == ParseMode.None) return@forEach
                checkLead(lead)
            }
            log.info { "completed lead check" }
            delay(1.minutes)
        }
    }

    /** Reads [lead], delivers what it finds, and records each page's outcome on its link. */
    private suspend fun checkLead(lead: Lead) {
        dao.updateCheckedAt(lead)

        val originId = lead.initialUrl.toOriginId() ?: return
        val name = if (lead is EventFeed) lead.name else "${lead.leadType}: ${lead.initialUrl}"

        val tracker = ParseTracker(name, lead.initialUrl)
        with (tracker) {
            try {
                crawl(lead)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error(e) { "check failed for $name" }
                tracker.failed(e)
            }
            tracker.records().forEach { dao.recordLink(it) }
            if (tracker.needsReport()) tracker.write(originId)
        }
    }

    fun logProblem(problem: Problem) {
        log.info { problem.message }
    }

    fun logProblem(message: String) = logProblem(Problem(message))
}

fun CoroutineScope.startCrawler(server: Server, client: HtmlParserClient) {
    launch {
        val fetcher = PageFetcher(server.dao)
        try {
            Crawler(server, client, fetcher).start()
        } finally {
            fetcher.close()
        }
    }
}

private val checkInterval = 24.hours
internal const val lmRetryCount = 10
