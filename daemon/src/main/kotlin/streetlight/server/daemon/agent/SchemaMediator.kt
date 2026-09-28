package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema
import streetlight.model.data.Origin
import streetlight.model.data.SelectorSchema
import streetlight.server.model.DaoFacade
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/** Receives what the [SchemaMediator] does for one page, around its requests to the LM. */
interface SchemaObserver : HtmlParseObserver {
    /** A request of [kind] is about to be made to the LM. */
    fun requested(kind: String) {}

    /** A stored [schema] was tried on the page, and whether it was [chosen]. */
    fun storedSchemaTried(schema: SelectorSchema, chosen: Boolean) {}

    /** A [schema] from the LM, and the outcome of its validation against the page. */
    fun schemaCreated(schema: SelectorSchema, validated: Outcome<SelectorSchema>) {}
}

/**
 * Finds the schema of a page: one stored for its origin that still reads it, or a new one from the LM, validated
 * against the page, refined with follow-up requests, and stored.
 */
class SchemaMediator(
    private val client: HtmlParserClient,
    private val dao: DaoFacade,
    private val retryCount: Int = 3,
) {
    private var isUsageLimitReached = false

    /**
     * The schema of the feed [doc] at [url] on [origin], asked of the LM with [instructions], with dates read in
     * [timeZoneId]. The LM is not asked once it has reached its usage limit.
     */
    suspend fun feedSchema(
        url: Url,
        doc: Document,
        origin: Origin,
        timeZoneId: String?,
        instructions: String = SchemaParserText.LocationFeedSelectorsInstructions,
        observer: SchemaObserver? = null,
    ): Outcome<EventFeedSchema> {
        val body = doc.body()
        dao.parser.read(origin.originId).sortedByDescending { it.lastSuccessAt }.forEach { parser ->
            val schema = parser.schema as? EventFeedSchema ?: return@forEach
            val selector = schema.event ?: return@forEach
            val isSuccess = body.tryQuery(selector).toDataOrNull()?.isNotEmpty() == true
            observer?.storedSchemaTried(schema, isSuccess)
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (isSuccess) return Ok(schema)
        }
        if (isUsageLimitReached) return LMProblem.UsageLimit

        observer?.requested(schemaRequest)
        val content = client.readHtml<ContentParse<EventFeedSchemaRequest>>(
            url, doc, instructions, retryCount, observer,
        ).toDataOr { return it.alsoNoteLimit() }
        val request = content.contentOr(origin) { return it }

        val schema = request.toSchema()
        val validated = schema.validate(body)
        observer?.schemaCreated(schema, validated)
        val valid = validated.toDataOr { return SchemaProblem.Invalid }
        val refined = refineFeedStart(url, doc, valid, timeZoneOf(timeZoneId), observer)
        dao.parser.create(origin.originId, refined, origin.fetchMode)
        return Ok(refined)
    }

    /**
     * The schema of the event page [doc] at [url] on [origin], with its date read in [timeZoneId]. The LM is not
     * asked once it has reached its usage limit.
     */
    suspend fun pageSchema(
        url: Url,
        doc: Document,
        origin: Origin,
        timeZoneId: String?,
        observer: SchemaObserver? = null,
    ): Outcome<EventPageSchema> {
        val body = doc.body()
        dao.parser.read(origin.originId).sortedByDescending { it.lastSuccessAt }.mapNotNull { parser ->
            val schema = parser.schema as? EventPageSchema ?: return@mapNotNull null
            val isSuccess = body.queryElement(schema.title) { it.isPlausibleField() } != null
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (!isSuccess) {
                observer?.storedSchemaTried(schema, false)
                return@mapNotNull null
            }
            val length = body.queryElement(schema.description) { it.isPlausibleProse() }?.html()?.length ?: 0
            length to schema
        }.maxByOrNull { it.first }?.let { (_, schema) ->
            observer?.storedSchemaTried(schema, true)
            return Ok(schema)
        }
        if (isUsageLimitReached) return LMProblem.UsageLimit

        observer?.requested(schemaRequest)
        val content = client.readHtml<ContentParse<EventPageSchemaRequest>>(
            url, doc, SchemaParserText.EventPageSelectorsInstructions, retryCount, observer,
        ).toDataOr { return it.alsoNoteLimit() }
        val request = content.contentOr(origin) { return it }

        val schema = request.toSchema()
        val validated = schema.validate(body)
        observer?.schemaCreated(schema, validated)
        val valid = validated.toDataOr { return SchemaProblem.Invalid }
        val started = refinePageStart(url, doc, valid, timeZoneOf(timeZoneId), observer)
        val refined = refinePageDescription(url, doc, started, observer)
        dao.parser.create(origin.originId, refined, origin.fetchMode)
        return Ok(refined)
    }

    /** The feed [schema] with the parts of its events' start, asked of the LM when the start does not already parse. */
    private suspend fun refineFeedStart(
        url: Url,
        doc: Document,
        schema: EventFeedSchema,
        zone: TimeZone,
        observer: SchemaObserver?,
    ): EventFeedSchema {
        val eventSelector = schema.event ?: return schema
        val events = doc.body().tryQuery(eventSelector).toDataOrNull()?.take(sampleSize) ?: return schema
        if (schema.startsParse(events, zone)) return schema

        observer?.requested(timeRequest)
        val request = client.readHtml<EventTimeSchemaRequest>(
            url, doc, SchemaParserText.eventFeedTimeInstructions(eventSelector), retryCount, observer,
        ).toDataOrNull() ?: return schema

        val refined = schema.copy(
            month = request.month.selectorOrNull()?.takeIf { events.allHold(it) { text -> text.hasMonthName() } },
            day = request.day.selectorOrNull()?.takeIf { events.allHold(it) { text -> text.isDayText() } && events.varies(it) },
            time = request.startTime.selectorOrNull()?.takeIf { events.allHold(it) { text -> parseTimeFromText(text) != null } }
                ?: schema.time,
        )
        return refined.takeIf { it.startsParse(events, zone) } ?: schema
    }

    /**
     * The page [schema] with its event's description, asked of the LM when it has none, kept only when the page reads
     * as prose through it.
     */
    private suspend fun refinePageDescription(
        url: Url,
        doc: Document,
        schema: EventPageSchema,
        observer: SchemaObserver?,
    ): EventPageSchema {
        if (schema.description != null) return schema

        observer?.requested(descriptionRequest)
        val request = client.readHtml<EventDescriptionSchemaRequest>(
            url, doc, SchemaParserText.EventPageDescriptionInstructions, retryCount, observer,
        ).toDataOrNull() ?: return schema

        val description = request.description.selectorOrNull()
            ?.takeIf { doc.body().queryElement(it) { element -> element.isPlausibleProse() } != null }
            ?: return schema
        return schema.copy(description = description)
    }

    /** The page [schema] with the parts of its event's start, asked of the LM when the start does not already parse. */
    private suspend fun refinePageStart(
        url: Url,
        doc: Document,
        schema: EventPageSchema,
        zone: TimeZone,
        observer: SchemaObserver?,
    ): EventPageSchema {
        val page = listOf(doc.body())
        if (schema.startsParse(page, zone)) return schema

        observer?.requested(timeRequest)
        val request = client.readHtml<EventTimeSchemaRequest>(
            url, doc, SchemaParserText.EventPageTimeInstructions, retryCount, observer,
        ).toDataOrNull() ?: return schema

        val refined = schema.copy(
            month = request.month.selectorOrNull()?.takeIf { page.allHold(it) { text -> text.hasMonthName() } },
            day = request.day.selectorOrNull()?.takeIf { page.allHold(it) { text -> text.isDayText() } },
            startTime = request.startTime.selectorOrNull()?.takeIf { page.allHold(it) { text -> parseTimeFromText(text) != null } }
                ?: schema.startTime,
        )
        return refined.takeIf { it.startsParse(page, zone) } ?: schema
    }
    /** This problem, noting when it is the LM's usage limit. */
    private fun Problem.alsoNoteLimit(): Problem = also { if (it == LMProblem.UsageLimit) isUsageLimitReached = true }

    /** The content of this parse, or the result of [onProblem]. An incomplete page marks [origin] for scripting. */
    private suspend inline fun <T> ContentParse<T>.contentOr(origin: Origin, onProblem: (Problem) -> Nothing): T {
        if (isIncompleteContent) dao.origin.registerIncomplete(origin.originId)
        val found = content
        if (!isExpectedContent || found == null) {
            onProblem(if (isIncompleteContent) SchemaProblem.Incomplete else Problem("Document content was not the expected kind"))
        }
        return found
    }
}

/** The text of the start of the event in [element] read by this schema, from its date and its parts. */
fun EventFeedSchema.startText(element: Element): String? =
    startText(element, date, month, day, time)

/** The text of the start of the event on [body] read by this schema, from its date and its parts. */
fun EventPageSchema.startText(body: Element): String? =
    startText(body, date, month, day, startTime)

private fun startText(element: Element, vararg selectors: String?): String? = selectors
    .mapNotNull { selector -> element.queryElement(selector) { it.isPlausibleField() }?.text()?.trim() }
    .filter { it.isNotEmpty() }
    .distinct()
    .joinToString(" ")
    .ifEmpty { null }

/**
 * Whether the starts read from [events] are sound: at least half parse, they run in list order, and they fall
 * within a year of now.
 */
private fun List<Element>.startsHold(zone: TimeZone, text: (Element) -> String?): Boolean {
    val starts = mapNotNull { element -> text(element)?.let { parseLocalDateTime(it, zone.id) } }
        .map { it.toInstant(zone) }
    if (starts.size * 2 < size || starts.isEmpty()) return false
    val now = Clock.System.now()
    val isPlausible = starts.all { it in (now - plausibleSpan)..(now + plausibleSpan) }
    val isOrdered = starts.zipWithNext().all { (first, second) -> first <= second }
    return isPlausible && isOrdered
}

private fun EventFeedSchema.startsParse(events: List<Element>, zone: TimeZone) =
    events.startsHold(zone) { startText(it) }

private fun EventPageSchema.startsParse(page: List<Element>, zone: TimeZone) =
    page.startsHold(zone) { startText(it) }

/** Whether [selector] matches in at least half of these elements, and every text it matches passes [test]. */
private fun List<Element>.allHold(selector: String, test: (String) -> Boolean): Boolean {
    val texts = mapNotNull { it.queryElement(selector)?.text()?.trim() }
    return texts.size * 2 >= size && texts.isNotEmpty() && texts.all(test)
}

/** Whether [selector] yields more than one distinct text across these elements, or matches a single one. */
private fun List<Element>.varies(selector: String): Boolean {
    val texts = mapNotNull { it.queryElement(selector)?.text()?.trim() }
    return texts.size < 2 || texts.distinct().size > 1
}

/** Whether this text holds a single day of the month, 1 to 31, with no other number. */
private fun String.isDayText(): Boolean {
    val numbers = numberPattern.findAll(this).map { it.value }.toList()
    val day = numbers.singleOrNull()?.takeIf { it.length <= 2 }?.toIntOrNull() ?: return false
    return day in 1..31 && length <= maxDayTextLength
}


private fun timeZoneOf(id: String?): TimeZone = id?.let { runCatching { TimeZone.of(it) }.getOrNull() } ?: TimeZone.UTC

private const val schemaRequest = "schema"
private const val timeRequest = "time"
private const val descriptionRequest = "description"
private const val sampleSize = 10
private const val maxDayTextLength = 24
private val plausibleSpan = 400.days
private val numberPattern = Regex("""\d+""")
