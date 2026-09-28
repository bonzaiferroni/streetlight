package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Problem
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import kampfire.utils.fuzzyMatches
import kampfire.utils.similarity
import klutch.server.provide
import kotlinx.coroutines.CancellationException
import streetlight.model.data.LocationLead
import streetlight.model.data.LocationSchema
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.SchemaType
import streetlight.model.data.SelectorSchema
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEdit
import streetlight.model.external.OSMQuery
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.tryQuery
import streetlight.server.db.datascope.createLocation
import streetlight.server.model.MapReferenceClient
import streetlight.server.utils.readImageUrl

/** Delivers the location [schema] reads from the homepage of [lead] fetched as [document]. */
context(tracker: ParseTracker)
suspend fun Crawler.crawlLocationLead(lead: LocationLead, document: FetchDocument?, schema: SelectorSchema?) {
    val locationSchema = schema as? LocationSchema ?: return
    if (document == null) return
    tracker.read(lead.initialUrl, SchemaType.Location)
    val location = parseLocation(locationSchema, document.doc)
    deliverLocation(lead, location)
}

/**
 * Delivers the location of [lead] described by [location]: placed on the map by its name and address, and created
 * unless it is already stored or cannot be placed.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.deliverLocation(lead: LocationLead, location: PropertyMap) {
    tracker.recordFound()
    val name = location[ParseProperty.Name] ?: return tracker.recordUnnamed(location)
    val text = listOfNotNull(name, location[ParseProperty.Address]).joinToString(", ")
    val hits = try {
        server.provide<MapReferenceClient>().search(OSMQuery(query = text)).orEmpty()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        return tracker.recordFailed(location, name, Problem("Map search failed: ${e.message}"))
    }
    val place = hits
        .filter { it.placeRank >= minPlaceRank && it.name?.fuzzyMatches(name) == true }
        .maxByOrNull { it.name?.similarity(name) ?: 0.0 }
        ?: return tracker.recordFailed(location, name, Problem("No place on the map matches $text"))

    dao.location.readLocationByMapId(place.osmId)?.let { return tracker.recordDuplicate(location, name, it.label) }

    val edit = location.toLocationEdit(lead.initialUrl).copy(address = null).mergeLeft(place.toEdit())
    if (!edit.validity.isValid || edit.state == null) {
        return tracker.recordFailed(location, name, Problem("The place on the map lacks what a location needs"))
    }
    try {
        server.createLocation(null, edit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Problem("${e::class.simpleName}: ${e.message}")
    }.toDataOr { return tracker.recordFailed(location, name, it) }
    tracker.recordCreated()
}

/** The properties of the location on its homepage [doc], read by [schema]. */
private fun parseLocation(schema: LocationSchema, doc: Document): PropertyMap = listOf(
    ParseProperty.Name to doc.queryElement(schema.name).plainText(),
    ParseProperty.Description to doc.queryElement(schema.description) { it.isPlausibleProse(allowsChrome = true) }.innerHtml(),
    ParseProperty.Address to doc.queryElement(schema.address).plainText(),
    ParseProperty.Phone to doc.queryElement(schema.phone).plainText(),
    ParseProperty.Email to doc.queryElement(schema.email).plainText(),
    ParseProperty.Hours to doc.queryElement(schema.hours).plainText(),
    ParseProperty.EventsLink to doc.queryElement(schema.eventsLink).absoluteUrl("href"),
    ParseProperty.Image to (doc.readImageUrl()?.value ?: doc.queryElement(schema.image).absoluteUrl("src")),
    ParseProperty.SocialLinks to schema.socialLinks?.let { selector ->
        doc.tryQuery(selector).toDataOrNull()?.mapNotNull { it.absoluteUrl("href") }?.distinct()?.joinToString("\n")
    }?.takeIf { it.isNotEmpty() },
).mapNotNull { (property, text) -> text?.let { property to it } }.toMap()

private const val minPlaceRank = 30
