package streetlight.server.daemon

import io.github.oshai.kotlinlogging.KotlinLogging
import kampfire.api.Slug
import kampfire.api.toMarkdown
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.model.toUrl
import klutch.server.provide
import koala.Image
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import streetlight.agent.KoogHtmlParserClient
import streetlight.agent.StreetlightAgent
import streetlight.agent.fetchText
import streetlight.model.data.EventEdit
import streetlight.model.data.Galaxy
import streetlight.model.data.LocationConfigContent
import streetlight.model.data.LocationId
import streetlight.model.data.Origin
import streetlight.model.data.OriginId
import streetlight.model.data.ParseMode
import streetlight.model.data.PostEdit
import streetlight.model.data.PostType
import streetlight.model.data.toOriginId
import streetlight.server.model.Server
import streetlight.server.plugins.logger
import streetlight.server.routes.createEvent
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ParseDaemon(private val server: Server) {

    val dao = server.dao
    val koog = server.provide<KoogHtmlParserClient>()
    val log = KotlinLogging.logger(ParseDaemon::class)

    var startedAt = Instant.DISTANT_PAST
        private set
    var lmUsageLimitReached = false

    private val gateMutex = Mutex()
    private val robotGates = mutableMapOf<OriginId, RobotGate>()

    suspend fun start() {
        while (true) {
            val locations = dao.location.readCheckable(checkInterval - 1.hours)
            locations.forEach { location ->
                if (location.config.parseMode == ParseMode.None) return@forEach
                checkLocation(location)
            }
            log.info { "completed location check" }
            delay(10.minutes)
        }
    }

    @JvmName("parseDaemonGetRobotGate")
    suspend fun getRobotGate(origin: Origin): RobotGate = gateMutex.withLock {
        robotGates.getOrPut(origin.originId) { origin.getRobotGate() }
    }

    /** Reads the feed of a location and creates its new events, then writes the location's parse log. */
    private suspend fun checkLocation(config: LocationConfigContent) {
        val location = config.location
        dao.location.updateCheckedAt(location.locationId)

        val feedUrl = requireNotNull(location.eventsUrl)
        val originId = feedUrl.toOriginId() ?: return

        val origin = dao.origin.readOrCreateOrigin(originId)

        val tracker = ParseTracker(location.slug.toString(), feedUrl.normalize())
        val reader = EventFeedReader(config, origin, feedUrl.normalize(), this, tracker)
        reader.read()?.forEach { event ->
            tracker.eventFound()
            val edit = event.toEventEdit(location.timezoneId, location.locationId)
            val startsAt = edit.startsAt ?: run {
                tracker.eventUnparsed(event)
                return@forEach
            }
            if (startsAt < Clock.System.now()) {
                tracker.eventPast()
                return@forEach
            }
            val existingEvent = dao.event.readEventAt(location.locationId, startsAt)
            if (existingEvent != null) {
                tracker.eventDuplicate()
                return@forEach
            }
            server.createEvent(null, edit).toDataOr {
                tracker.eventFailed()
                return@forEach
            }
            tracker.eventCreated()
        }
        tracker.report().write(originId)
    }

    fun logProblem(problem: Problem) {
        log.info { problem.message }
    }

    fun logProblem(message: String) = logProblem(Problem(message))

    private suspend fun Origin.getRobotGate(): RobotGate {
        robotsTxt?.let {
            return it.toRobotGate()
        }
        val txt = fetchText(originId.toRobotsTxtUrl()).toDataOrNull()?.text
            ?: return RobotGate(null, StreetlightAgent.AgentToken)
        dao.origin.updateRobotsTxt(originId, txt)
        return txt.toRobotGate()
    }
}

fun CoroutineScope.startParseDaemon(server: Server) {
    launch {
        ParseDaemon(server).start()
        closeBrowser()
    }
}

data class RawEvent(
    val title: String? = null,
    val url: Url? = null,
    val image: String? = null,
    val descriptionHtml: String? = null,
    val contact: String? = null,
    val cost: String? = null,
    val ageMin: String? = null,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
)

private val checkInterval = 24.hours
internal const val lmRetryCount = 10

private fun RawEvent.toEventEdit(timeZoneId: String?, locationId: LocationId): EventEdit {

    val dateTimeText = listOfNotNull(date, startTime.takeIf { it != date })
        .joinToString(" ")
        .takeIf { it.isNotBlank() }

    val description = descriptionHtml?.let { htmlToMarkdown(it) }
    val start = dateTimeText?.let { parseLocalDateTime(it, timeZoneId) }
    val end = endTime?.let { parseTimeFromText(it) }

    // val notes = listOfNotNull(
    //     end?.let { "* **Ends:** $it" },
    //     cost?.let { "* **Cost:** $it" },
    //     ageMin?.let { "* **Ages:** $it" },
    // )
    // td: escape markdown
    val timeNote = if (start == null) (startTime ?: date)?.let { "**Time:** $it" } else null

    val body = listOfNotNull(
        timeNote,
        description,
        // notes.joinToString("\n").takeIf { it.isNotBlank() },
    ).joinToString("\n\n").takeIf { it.isNotBlank() }

    return EventEdit(
        locationId = locationId,
        title = title,
        description = body?.toMarkdown(),
        contact = contact, // td: gather phone/email/social media separately
        website = url,
        image = image?.let { Image(it.toUrl()) },
        date = start?.date,
        startTime = start?.time,
        endTime = end,
        timeZoneId = timeZoneId,
        // td: parse ageMin
    )
}

sealed interface FieldResult {
    data object Absent : FieldResult
    data class Invalid(val text: String, val reason: String) : FieldResult
    data class Valid(val value: String, val confidence: Double) : FieldResult
}

//         val chromeFields = setOfNotNull(
//            "descriptionHtml".takeIf { gathered.isConstant { event -> event.descriptionHtml } },
//            "title".takeIf { gathered.isConstant { event -> event.title } },
//            "date".takeIf { gathered.isConstant { event -> event.date } },
//        )
//        if (chromeFields.isNotEmpty()) println("suspected chrome, identical across feed: $chromeFields")
//
//        gathered.forEach { raw ->
//            val judged = raw.copy(
//                descriptionHtml = raw.descriptionHtml.takeIf { "descriptionHtml" !in chromeFields },
//                title = raw.title.takeIf { "title" !in chromeFields },
//                date = raw.date.takeIf { "date" !in chromeFields },
//            )
//            val edit = judged.toEventEdit(null, location.timezoneId, location.locationId)
//            println(prettyPrint(edit))
//        }