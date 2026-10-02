package streetlight.server.daemon.crawler

import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.model.Problem
import klutch.server.provide
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import streetlight.server.daemon.agent.EntityClassifier
import streetlight.server.daemon.agent.HtmlParserClient
import streetlight.server.daemon.agent.SchemaMediator
import streetlight.model.data.EventFeed
import streetlight.model.data.Lead
import streetlight.server.model.MapReferenceClient
import streetlight.server.model.Server
import streetlight.server.plugins.logger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * The reader of each feed that is due, fetching its pages with [fetcher], asking the LM through [client] for the
 * schemas it needs, and classifying events with [classifier].
 */
class Crawler(
    val server: Server,
    client: HtmlParserClient,
    val classifier: EntityClassifier,
    val fetcher: PageFetcher,
) {

    val dao = server.dao
    val mediator = SchemaMediator(client, dao, lmRetryCount)
    val spawner = LocationSpawner(server, server.provide<MapReferenceClient>(), OSMGate())
    val log = KotlinLogging.logger(Crawler::class)
    @PublishedApi internal val writeMutex = Mutex()
    private val workers = Semaphore(maxWorkers)

    /**
     * Reads the leads due, up to [maxWorkers] at once, each in its own coroutine: every second, as many more as there
     * is room for are marked checked and launched.
     */
    suspend fun start(): Unit = coroutineScope {
        var noWorkLastCheck = false
        while (true) {
            val workersAvailable = workers.availablePermits
            if (workersAvailable > 0) {
                val leads = dao.readCheckable(checkInterval - 1.hours, workersAvailable)

                leads.forEach { lead ->
                    workers.acquire()
                    dbWrite { dao.updateCheckedAt(lead) }
                    launch {
                        try {
                            checkLead(lead)
                        } finally {
                            workers.release()
                        }
                    }
                }

                val noWork = workers.availablePermits == maxWorkers
                if (!noWorkLastCheck && noWork) {
                    log.info { "Completed lead check" }
                }
                noWorkLastCheck = noWork
            }
            delay(2.seconds)
        }
    }

    /** Reads [lead], already marked checked, delivers what it finds, and records each page's outcome on its link. */
    private suspend fun checkLead(lead: Lead) {
        val name = if (lead is EventFeed) lead.name else "${lead.leadType}: ${lead.initialUrl}"

        val tracker = ParseTracker(name, lead.initialUrl)
        with (tracker) {
            try {
                crawl(lead)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log.error(e) { "check failed for $name" }
                tracker.trackFailedCheck(e)
            }
            dbWrite { tracker.records().forEach { dao.recordLink(it) } }
            if (tracker.needsReport()) tracker.write(lead.leadType)
        }
    }

    /**
     * The result of [block], run while no other of the crawler's database writes runs. A block holds the reads its
     * write depends on, never nests another, and does no slow work.
     */
    suspend inline fun <T> dbWrite(block: () -> T): T = writeMutex.withLock(action = block)

    fun logProblem(problem: Problem) {
        log.info { problem.message }
    }

    fun logProblem(message: String) = logProblem(Problem(message))
}

fun CoroutineScope.startCrawler(server: Server, client: HtmlParserClient, classifier: EntityClassifier) {
    launch {
        val fetcher = PageFetcher(server.dao)
        try {
            Crawler(server, client, classifier, fetcher).start()
        } finally {
            fetcher.close()
        }
    }
}

private val checkInterval = 24.hours
private const val maxWorkers = 8
internal const val lmRetryCount = 10
