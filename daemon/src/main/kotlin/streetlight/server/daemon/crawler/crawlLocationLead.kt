package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Problem
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import streetlight.model.data.LocationLead
import streetlight.model.data.LmSchema
import streetlight.model.data.LocationSchema
import streetlight.model.data.LocationSelectorSchema
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.SchemaType
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.readPageLdPlace
import streetlight.server.daemon.agent.resolveUrl
import streetlight.server.daemon.agent.toPropertyMap
import streetlight.server.daemon.agent.tryQuery
import streetlight.server.utils.readImageUrl
import streetlight.server.utils.readMetaContent

/**
 * Delivers the location the LM read from the homepage of [lead] fetched as [document], as [schema], with the values
 * the place its JSON-LD declares laid over it.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlLocationLead(lead: LocationLead, document: FetchDocument?, schema: LmSchema?) {
    val locationSchema = schema as? LocationSchema ?: return
    if (document == null) return
    tracker.read(lead.initialUrl, SchemaType.Location)
    val location = listOf(
        ParseProperty.Name to locationSchema.name,
        ParseProperty.Description to locationSchema.description,
        ParseProperty.Address to locationSchema.address,
        ParseProperty.Phone to locationSchema.phone,
        ParseProperty.Email to locationSchema.email,
        ParseProperty.Hours to locationSchema.hours,
        ParseProperty.EventsLink to locationSchema.eventsUrl?.let { document.doc.resolveUrl(it) },
        ParseProperty.Image to (document.doc.readImageUrl()?.value ?: locationSchema.imageUrl?.let { document.doc.resolveUrl(it) }),
    ).mapNotNull { (property, text) -> text?.takeIf { it.isNotBlank() }?.let { property to it } }.toMap()
    val ldPlace = document.doc.readPageLdPlace()
    val declaredLocation = ldPlace?.toPropertyMap().orEmpty()
    if (declaredLocation.isNotEmpty()) tracker.declared(lead.initialUrl)
    val area = listOfNotNull(
        ldPlace?.locality ?: locationSchema.city,
        ldPlace?.region ?: locationSchema.state,
        ldPlace?.postalCode ?: locationSchema.postalCode,
    ).joinToString(" ").ifEmpty { null }
    deliverLocation(lead, location + declaredLocation, area, document.doc.readMetaContent("og:site_name"))
}

/**
 * Delivers the location of [lead] described by [location]: placed on the map by its name, address and [area], or by
 * the [declaredName] its page gives itself when its name finds no place, or at its address alone, and created unless
 * it is already stored or cannot be placed.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.deliverLocation(
    lead: LocationLead,
    location: PropertyMap,
    area: String?,
    declaredName: String?,
) {
    tracker.recordFound()
    val name = location[ParseProperty.Name] ?: declaredName ?: return tracker.recordUnnamed(location)
    val address = location[ParseProperty.Address]
    spawner.readStoredAt(name, address)?.let { return tracker.recordDuplicate(location, name, it.label) }
    val (place, placedName) = spawner.findPlace(lead.initialUrl, name, declaredName, address, area)
        ?: return deliverAtAddress(lead, location, name, declaredName, address, area)
    dao.location.readLocationByMapId(place.osmId)?.let { return tracker.recordDuplicate(location, placedName, it.label) }
    spawner.createFrom(place, location + (ParseProperty.Name to placedName), lead.initialUrl)
        .toDataOr { return tracker.recordFailed(location, placedName, it) }
    tracker.recordCreated()
}

/**
 * Delivers the location of [lead] described by [location], named [name], at its [address] in [area] when the map
 * knows the address but not the name: stored there already, or created there with the page's details.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.deliverAtAddress(
    lead: LocationLead,
    location: PropertyMap,
    name: String,
    declaredName: String?,
    address: String?,
    area: String?,
) {
    val place = address?.let { spawner.findAddress(it, area) } ?: run {
        val searched = listOfNotNull(name, declaredName, address, area).distinct().joinToString(" / ")
        return tracker.recordFailed(location, name, Problem("No place on the map matches $searched"))
    }
    spawner.readStored(place, name)?.let { return tracker.recordDuplicate(location, name, it.label) }
    spawner.createNamedAt(place, location + (ParseProperty.Name to name), lead.initialUrl)
        .toDataOr { return tracker.recordFailed(location, name, it) }
    tracker.recordCreated()
}

/** The properties of the location on its homepage [doc], read by selector with [schema]. */
private fun parseLocation(schema: LocationSelectorSchema, doc: Document): PropertyMap = listOf(
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
