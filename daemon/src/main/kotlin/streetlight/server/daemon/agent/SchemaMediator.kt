package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.Url
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema
import streetlight.model.data.EventRead
import streetlight.model.data.FetchMode
import streetlight.model.data.LocationRead
import streetlight.model.data.LocationSelectorSchema
import streetlight.model.data.Origin
import streetlight.model.data.ParserId
import streetlight.model.data.SelectorSchema
import streetlight.server.model.DaoFacade
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/** Receives what the [SchemaMediator] does for one page, around its requests to the LM. */
interface SchemaObserver : HtmlParseObserver {
    /** A request of [kind] is about to be made to the LM. */
    fun requested(kind: String) {}

    /** A stored [schema] was tried on the page, and whether it was [chosen]. */
    fun storedSchemaTried(schema: SelectorSchema, chosen: Boolean) {}

    /** A [schema] from the LM, and the outcome of its validation against the page. */
    fun schemaCreated(schema: SelectorSchema, validatedSchema: Outcome<SelectorSchema>) {}
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
    private val requestMutex = Mutex()

    /**
     * The schema of the feed [doc] at [url] on [origin], asked of the LM with [instructions], with dates read in
     * [timeZoneId]. The LM is not asked once it has reached its usage limit.
     */
    suspend fun feedSchema(
        url: Url,
        doc: Document,
        origin: Origin,
        fetchMode: FetchMode,
        timeZoneId: String?,
        instructions: String = SchemaParserText.LocationFeedSelectorsInstructions,
        observer: SchemaObserver? = null,
    ): Outcome<EventFeedSchema> {
        val triedIds = mutableSetOf<ParserId>()
        storedFeedSchema(doc, origin, triedIds, observer)?.let { return Ok(it) }
        return requestSchema {
            storedFeedSchema(doc, origin, triedIds, observer)?.let { return Ok(it) }
            if (isUsageLimitReached) return LMProblem.UsageLimit

            observer?.requested(schemaRequest)
            val request = readContent<EventFeedSchemaRequest>(url, doc, origin, fetchMode, instructions, observer) { return it }

            val schema = request.toSchema()
            val validatedSchema = schema.validate(doc)
            observer?.schemaCreated(schema, validatedSchema)
            val validSchema = validatedSchema.toDataOr { return SchemaProblem.Invalid }
            val refinedSchema = refineFeedStart(url, doc, validSchema, timeZoneOf(timeZoneId), observer)
            dao.parser.create(origin.originId, refinedSchema, origin.fetchMode)
            Ok(refinedSchema)
        }
    }

    /**
     * The schema of the event page [doc] at [url] on [origin], with its date read in [timeZoneId]. The LM is not
     * asked once it has reached its usage limit.
     */
    suspend fun pageSchema(
        url: Url,
        doc: Document,
        origin: Origin,
        fetchMode: FetchMode,
        timeZoneId: String?,
        observer: SchemaObserver? = null,
    ): Outcome<EventPageSchema> {
        val triedIds = mutableSetOf<ParserId>()
        storedPageSchema(doc, origin, triedIds, observer)?.let { return Ok(it) }
        return requestSchema {
            storedPageSchema(doc, origin, triedIds, observer)?.let { return Ok(it) }
            if (isUsageLimitReached) return LMProblem.UsageLimit

            observer?.requested(schemaRequest)
            val request = readContent<EventPageSchemaRequest>(
                url, doc, origin, fetchMode, SchemaParserText.EventPageSelectorsInstructions, observer,
            ) { return it }

            val schema = request.toSchema()
            val validatedSchema = schema.validate(doc)
            observer?.schemaCreated(schema, validatedSchema)
            val validSchema = validatedSchema.toDataOr { return SchemaProblem.Invalid }
            val startedSchema = refinePageStart(url, doc, validSchema, timeZoneOf(timeZoneId), observer)
            val refinedSchema = refinePageDescription(url, doc, startedSchema, observer)
            dao.parser.create(origin.originId, refinedSchema, origin.fetchMode)
            Ok(refinedSchema)
        }
    }

    /**
     * The schema of the location homepage [doc] at [url] on [origin]. The LM is not asked once it has reached its
     * usage limit.
     */
    suspend fun locationSchema(
        url: Url,
        doc: Document,
        origin: Origin,
        fetchMode: FetchMode,
        observer: SchemaObserver? = null,
    ): Outcome<LocationSelectorSchema> {
        val triedIds = mutableSetOf<ParserId>()
        storedLocationSchema(doc, origin, triedIds, observer)?.let { return Ok(it) }
        return requestSchema {
            storedLocationSchema(doc, origin, triedIds, observer)?.let { return Ok(it) }
            if (isUsageLimitReached) return LMProblem.UsageLimit

            observer?.requested(schemaRequest)
            val request = readContent<LocationSchemaRequest>(
                url, doc, origin, fetchMode, SchemaParserText.LocationSelectorsInstructions, observer,
            ) { return it }

            val schema = request.toSchema()
            val validatedSchema = schema.validate(doc)
            observer?.schemaCreated(schema, validatedSchema)
            val validSchema = validatedSchema.toDataOr { return SchemaProblem.Invalid }
            dao.parser.create(origin.originId, validSchema, origin.fetchMode)
            Ok(validSchema)
        }
    }

    /**
     * The details of a location, read directly by the LM from its homepage [doc] at [url] on [origin], and not
     * stored. The LM is not asked once it has reached its usage limit.
     */
    suspend fun readLocation(
        url: Url,
        doc: Document,
        origin: Origin,
        fetchMode: FetchMode,
        observer: SchemaObserver? = null,
    ): Outcome<LocationRead> = requestSchema {
        if (isUsageLimitReached) return LMProblem.UsageLimit
        observer?.requested(readRequest)
        Ok(readContent<LocationRead>(url, doc, origin, fetchMode, SchemaParserText.LocationInstructions, observer) { return it })
    }

    /**
     * The details of an event and its place, read directly by the LM from its page [doc] at [url] on [origin], and not
     * stored. The LM is not asked once it has reached its usage limit.
     */
    suspend fun readEvent(
        url: Url,
        doc: Document,
        origin: Origin,
        fetchMode: FetchMode,
        observer: SchemaObserver? = null,
    ): Outcome<EventRead> = requestSchema {
        if (isUsageLimitReached) return LMProblem.UsageLimit
        observer?.requested(readRequest)
        Ok(readContent<EventRead>(url, doc, origin, fetchMode, SchemaParserText.EventInstructions, observer) { return it })
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
        val events = doc.tryQuery(eventSelector).toDataOrNull()?.take(sampleSize) ?: return schema
        if (schema.startsParse(events, zone)) return schema

        observer?.requested(timeRequest)
        val request = client.readHtml<EventTimeSchemaRequest>(
            url, doc, SchemaParserText.eventFeedTimeInstructions(eventSelector), retryCount, observer,
        ).toDataOrNull() ?: return schema

        val refinedSchema = schema.copy(
            month = request.month.selectorOrNull()?.takeIf { events.allHold(it) { text -> text.hasMonthName() } },
            day = request.day.selectorOrNull()?.takeIf { events.allHold(it) { text -> text.isDayText() } && events.varies(it) },
            time = request.startTime.selectorOrNull()?.takeIf { events.allHold(it) { text -> parseTimeFromText(text) != null } }
                ?: schema.time,
        )
        return refinedSchema.takeIf { it.startsParse(events, zone) } ?: schema
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
            ?.takeIf { doc.queryElement(it) { element -> element.isPlausibleProse() } != null }
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
        val page = listOf(doc)
        if (schema.startsParse(page, zone)) return schema

        observer?.requested(timeRequest)
        val request = client.readHtml<EventTimeSchemaRequest>(
            url, doc, SchemaParserText.EventPageTimeInstructions, retryCount, observer,
        ).toDataOrNull() ?: return schema

        val refinedSchema = schema.copy(
            month = request.month.selectorOrNull()?.takeIf { page.allHold(it) { text -> text.hasMonthName() } },
            day = request.day.selectorOrNull()?.takeIf { page.allHold(it) { text -> text.isDayText() } },
            startTime = request.startTime.selectorOrNull()?.takeIf { page.allHold(it) { text -> parseTimeFromText(text) != null } }
                ?: schema.startTime,
        )
        return refinedSchema.takeIf { it.startsParse(page, zone) } ?: schema
    }
    /** The result of [block], run while no other of the mediator's requests to the LM runs. */
    private suspend inline fun <T> requestSchema(block: () -> T): T = requestMutex.withLock(action = block)

    /** The first stored feed schema of [origin] whose event selector matches [doc], skipping and adding to [triedIds]. */
    private suspend fun storedFeedSchema(
        doc: Document,
        origin: Origin,
        triedIds: MutableSet<ParserId>,
        observer: SchemaObserver?,
    ): EventFeedSchema? {
        dao.parser.read(origin.originId).sortedByDescending { it.lastSuccessAt }.forEach { parser ->
            if (!triedIds.add(parser.parserId)) return@forEach
            val schema = parser.schema as? EventFeedSchema ?: return@forEach
            val selector = schema.event ?: return@forEach
            val isSuccess = doc.tryQuery(selector).toDataOrNull()?.isNotEmpty() == true
            observer?.storedSchemaTried(schema, isSuccess)
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (isSuccess) return schema
        }
        return null
    }

    /**
     * The stored page schema of [origin] whose title passes on [doc], the one with the longest description, skipping
     * and adding to [triedIds].
     */
    private suspend fun storedPageSchema(
        doc: Document,
        origin: Origin,
        triedIds: MutableSet<ParserId>,
        observer: SchemaObserver?,
    ): EventPageSchema? {
        val best = dao.parser.read(origin.originId).sortedByDescending { it.lastSuccessAt }.mapNotNull { parser ->
            if (!triedIds.add(parser.parserId)) return@mapNotNull null
            val schema = parser.schema as? EventPageSchema ?: return@mapNotNull null
            val isSuccess = doc.queryElement(schema.title) { it.isPlausibleField() } != null
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (!isSuccess) {
                observer?.storedSchemaTried(schema, false)
                return@mapNotNull null
            }
            val length = doc.queryElement(schema.description) { it.isPlausibleProse() }?.html()?.length ?: 0
            length to schema
        }.maxByOrNull { it.first }?.second ?: return null
        observer?.storedSchemaTried(best, true)
        return best
    }

    /** The first stored location schema of [origin] whose name matches [doc], skipping and adding to [triedIds]. */
    private suspend fun storedLocationSchema(
        doc: Document,
        origin: Origin,
        triedIds: MutableSet<ParserId>,
        observer: SchemaObserver?,
    ): LocationSelectorSchema? {
        dao.parser.read(origin.originId).sortedByDescending { it.lastSuccessAt }.forEach { parser ->
            if (!triedIds.add(parser.parserId)) return@forEach
            val schema = parser.schema as? LocationSelectorSchema ?: return@forEach
            val isSuccess = doc.queryElement(schema.name) != null
            observer?.storedSchemaTried(schema, isSuccess)
            dao.parser.updateResult(parser.parserId, isSuccess)
            if (isSuccess) return schema
        }
        return null
    }

    /** This problem, noting when it is the LM's usage limit. */
    private fun Problem.alsoNoteLimit(): Problem = also { if (it == LMProblem.UsageLimit) isUsageLimitReached = true }

    /**
     * The content the LM reads with [instructions] from the page [doc] at [url] on [origin], fetched in [fetchMode],
     * or the result of [onProblem]. Only a page fetched without scripting is asked whether scripting is required, and
     * marks [origin] for scripting when it is.
     */
    private suspend inline fun <reified T : Any> readContent(
        url: Url,
        doc: Document,
        origin: Origin,
        fetchMode: FetchMode,
        instructions: String,
        observer: SchemaObserver?,
        onProblem: (Problem) -> Nothing,
    ): T {
        val prompt = instructions.replace(
            SchemaParserText.ContentObjectSlot, SchemaParserText.contentObjectInstructions(fetchMode),
        )
        if (fetchMode == FetchMode.Scripting) {
            val parse = client.readHtml<ScriptingContentParse<T>>(url, doc, prompt, retryCount, observer)
                .toDataOr { onProblem(it.alsoNoteLimit()) }
            return parse.content?.takeIf { parse.isExpectedContent } ?: onProblem(notExpected)
        }
        val parse = client.readHtml<BasicContentParse<T>>(url, doc, prompt, retryCount, observer)
            .toDataOr { onProblem(it.alsoNoteLimit()) }
        return parse.content?.takeIf { parse.isExpectedContent }
            ?: onProblem(if (parse.isScriptingRequired) SchemaProblem.ScriptingRequired else notExpected)
    }
}

/** The text of the start of the event in [element] read by this schema, from its date and its parts. */
fun EventFeedSchema.startText(element: Element): String? =
    startText(element, date, month, day, time)

/** The text of the start of the event on [document] read by this schema, from its date and its parts. */
fun EventPageSchema.startText(document: Element): String? =
    startText(document, date, month, day, startTime)

private fun startText(element: Element, vararg selectors: String?): String? = selectors
    .mapNotNull { selector -> element.queryElement(selector) { it.isPlausibleField() }.plainText() }
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
    val texts = mapNotNull { it.queryElement(selector).plainText() }
    return texts.size * 2 >= size && texts.isNotEmpty() && texts.all(test)
}

/** Whether [selector] yields more than one distinct text across these elements, or matches a single one. */
private fun List<Element>.varies(selector: String): Boolean {
    val texts = mapNotNull { it.queryElement(selector).plainText() }
    return texts.size < 2 || texts.distinct().size > 1
}

/** Whether this text holds a single day of the month, 1 to 31, with no other number. */
private fun String.isDayText(): Boolean {
    val numbers = numberPattern.findAll(this).map { it.value }.toList()
    val day = numbers.singleOrNull()?.takeIf { it.length <= 2 }?.toIntOrNull() ?: return false
    return day in 1..31 && length <= maxDayTextLength
}


private fun timeZoneOf(id: String?): TimeZone = id?.let { runCatching { TimeZone.of(it) }.getOrNull() } ?: TimeZone.UTC

private val notExpected = Problem("Document content was not the expected kind")
private const val schemaRequest = "schema"
private const val timeRequest = "time"
private const val descriptionRequest = "description"
private const val readRequest = "read"
private const val sampleSize = 10
private const val maxDayTextLength = 24
private val plausibleSpan = 400.days
private val numberPattern = Regex("""\d+""")
