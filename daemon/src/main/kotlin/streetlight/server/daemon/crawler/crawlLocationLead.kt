package streetlight.server.daemon.crawler

import com.fleeksoft.ksoup.nodes.Document
import kampfire.model.Problem
import kotlinx.coroutines.CancellationException
import kampfire.model.toDataOr
import kampfire.model.toDataOrNull
import streetlight.model.data.LocationId
import streetlight.model.data.LocationLead
import streetlight.model.data.mergeLeft
import streetlight.model.data.toEdit
import streetlight.model.data.LmSchema
import streetlight.model.data.LocationRead
import streetlight.model.data.LocationSelectorSchema
import streetlight.model.data.ParseProperty
import streetlight.model.data.PropertyMap
import streetlight.model.data.buildPropertyMap
import streetlight.model.data.SchemaType
import streetlight.server.daemon.agent.absoluteUrl
import streetlight.server.daemon.agent.imageUrl
import streetlight.server.daemon.agent.innerHtml
import streetlight.server.daemon.agent.isPlausibleProse
import streetlight.server.daemon.agent.plainText
import streetlight.server.daemon.agent.queryElement
import streetlight.server.daemon.agent.toPropertyMap
import streetlight.server.daemon.agent.readPageLdPlace
import streetlight.server.daemon.agent.resolveUrl
import streetlight.server.daemon.agent.areaOf
import streetlight.server.daemon.agent.tryQuery
import streetlight.server.utils.readImageUrl
import streetlight.server.db.datascope.updateLocation
import streetlight.server.utils.readMetaContent

/**
 * Delivers the location the LM read from the homepage of [lead] fetched as [document], as [schema], with the values
 * the place its JSON-LD declares laid over it, or fills what the stored location of [lead] lacks with it.
 */
context(tracker: ParseTracker)
suspend fun Crawler.crawlLocationLead(lead: LocationLead, document: FetchDocument?, schema: LmSchema?) {
    val locationRead = schema as? LocationRead ?: return
    if (document == null) return
    tracker.trackReadUrl(lead.initialUrl, SchemaType.Location)

    val location = locationRead.parseLocation(document.doc)
    val ldPlace = document.doc.readPageLdPlace()
    val declaredLocation = ldPlace?.toPropertyMap().orEmpty()
    declaredLocation.trackDeclaredLd(lead.initialUrl)

    when (val locationId = lead.locationId) {
        null -> {
            val region = ldPlace?.region ?: locationRead.state
            val area = areaOf(ldPlace?.locality ?: locationRead.city, region, ldPlace?.postalCode ?: locationRead.postalCode)
            deliverLocation(lead, location + declaredLocation, area, region, document.doc.readMetaContent("og:site_name"))
        }
        else -> {
            mergeAndUpdateLocation(lead, locationId, location + declaredLocation)
        }
    }
}

/**
 * Fills what the stored location [locationId] lacks with the [location] read from the homepage of [lead], every
 * stored value kept. A location given nothing new is recorded as a duplicate.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.mergeAndUpdateLocation(lead: LocationLead, locationId: LocationId, location: PropertyMap) {
    tracker.trackFoundRecord()
    val name = location[ParseProperty.Name] ?: lead.initialUrl.value
    val stored = dao.location.readLocation(locationId, null)
        ?: return tracker.trackFailedRecord(location, name, Problem("No location is stored as $locationId"))

    val storedEdit = stored.toEdit()
    val edit = storedEdit.mergeLeft(location.toLocationEdit(lead.initialUrl))
    if (edit == storedEdit) return tracker.trackDuplicateRecord(location, name, stored.label)

    dbWrite {
        try {
            server.updateLocation(locationId, null, edit, isImageRequired = false)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Problem("${e::class.simpleName}: ${e.message}")
        }
    }.toDataOr { return tracker.trackFailedRecord(location, name, it) }
    tracker.trackUpdatedRecord()
}

/**
 * Delivers the location of [lead] described by [location]: placed on the map by its name, address and [area] or [region], or by
 * the [declaredName] its page gives itself when its name finds no place, or at its address alone, and created unless
 * it is already stored or cannot be placed.
 */
context(tracker: ParseTracker)
private suspend fun Crawler.deliverLocation(
    lead: LocationLead,
    location: PropertyMap,
    area: String?,
    region: String?,
    declaredName: String?,
) {
    tracker.trackFoundRecord()
    val name = location[ParseProperty.Name] ?: declaredName ?: return tracker.trackUnnamedRecord(location)
    val address = location[ParseProperty.Address]

    spawner.readLocationAt(name, address)?.let { return tracker.trackDuplicateRecord(location, name, it.label) }

    when (val found = spawner.findPlace(lead.initialUrl, name, declaredName, address, area, region)) {
        null -> deliverAtAddress(lead, location, name, declaredName, address, area, region)
        else -> {
            val (place, placedName) = found
            dao.location.readLocationByMapId(place.osmId)?.let { return tracker.trackDuplicateRecord(location, placedName, it.label) }
            spawner.createLocationFrom(place, location + (ParseProperty.Name to placedName), lead.initialUrl)
                .toDataOr { return tracker.trackFailedRecord(location, placedName, it) }
            tracker.trackCreatedRecord()
        }
    }
}

/**
 * Delivers the location of [lead] described by [location], named [name], at its [address] in [area] or [region] when the map
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
    region: String?,
) {
    val place = address?.let { spawner.findAddress(it, area, region) } ?: run {
        val searched = listOfNotNull(name, declaredName, address, area).distinct().joinToString(" / ")
        return tracker.trackFailedRecord(location, name, Problem("No place on the map matches $searched"))
    }

    spawner.readStoredLocation(place, name)?.let { return tracker.trackDuplicateRecord(location, name, it.label) }
    spawner.createNamedLocationAt(place, location + (ParseProperty.Name to name), lead.initialUrl)
        .toDataOr { return tracker.trackFailedRecord(location, name, it) }

    tracker.trackCreatedRecord()
}

/** The properties of the location the LM read from its homepage [doc], its links resolved against [doc]. */
private fun LocationRead.parseLocation(doc: Document): PropertyMap = buildPropertyMap {
    this[ParseProperty.Name] = name
    this[ParseProperty.Description] = description
    this[ParseProperty.Address] = address
    this[ParseProperty.Phone] = phone
    this[ParseProperty.Email] = email
    this[ParseProperty.Hours] = hours
    this[ParseProperty.EventsLink] = eventsUrl?.let { doc.resolveUrl(it) }
    this[ParseProperty.Image] = doc.readImageUrl(resolveIfRelative = true)?.value ?: imageUrl?.let { doc.resolveUrl(it) }
}

/** The properties of the location on its homepage [doc], read by selector with [schema]. */
private fun parseLocation(schema: LocationSelectorSchema, doc: Document): PropertyMap = buildPropertyMap {
    this[ParseProperty.Name] = doc.queryElement(schema.name).plainText()
    this[ParseProperty.Description] = doc.queryElement(schema.description) { it.isPlausibleProse(allowsChrome = true) }.innerHtml()
    this[ParseProperty.Address] = doc.queryElement(schema.address).plainText()
    this[ParseProperty.Phone] = doc.queryElement(schema.phone).plainText()
    this[ParseProperty.Email] = doc.queryElement(schema.email).plainText()
    this[ParseProperty.Hours] = doc.queryElement(schema.hours).plainText()
    this[ParseProperty.EventsLink] = doc.queryElement(schema.eventsLink).absoluteUrl("href")
    this[ParseProperty.Image] = doc.readImageUrl(resolveIfRelative = true)?.value ?: doc.queryElement(schema.image).imageUrl()
    this[ParseProperty.SocialLinks] = schema.socialLinks?.let { selector ->
        doc.tryQuery(selector).toDataOrNull()?.mapNotNull { it.absoluteUrl("href") }?.distinct()?.joinToString("\n")
    }
}

