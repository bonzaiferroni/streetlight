package streetlight.server.daemon.agent

import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.toDataOr
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema
import streetlight.model.data.LocationSchema

object SchemaProblem {
    val Invalid = Problem("The LM schema failed validation against its page.")
    val Incomplete = Problem("The page content is incomplete without scripting.")
}

/** Validates a feed schema from the LM against the [document] it was made from, setting any other selector that fails to null. */
fun EventFeedSchema.validate(document: Element): Outcome<EventFeedSchema> {
    val selector = event ?: return Problem("No event selector")
    val events = document.tryQuery(selector).toDataOr { return it }
    if (events.isEmpty()) return Problem("Event selector matched nothing: $selector")
    val page = listOf(document)
    return Ok(copy(
        feedLocation = feedLocation.keepIfMatches(page),
        address = address.keepIfMatches(page),
        title = title.keepIfMatches(events),
        eventLocation = eventLocation.keepIfMatches(events),
        link = link.keepIfMatches(events),
        image = image.keepIfMatches(events),
        cost = cost.keepIfMatches(events),
        description = description.keepIfMatches(events),
        date = date?.takeIf { readsAsDate(events, it) },
        time = time.keepIfMatches(events),
    ))
}

/** Validates a page schema from the LM against the [document] it was made from, setting any other selector that fails to null. */
fun EventPageSchema.validate(document: Element): Outcome<EventPageSchema> {
    if (document.queryElement(title) { it.isPlausibleField() } == null) return Problem("Title selector does not match: $title")
    val page = listOf(document)
    return Ok(copy(
        description = description.keepIfMatches(page) { it.isPlausibleProse() },
        location = location.keepIfMatches(page),
        address = address.keepIfMatches(page),
        image = image.keepIfMatches(page),
        cost = cost.keepIfMatches(page),
        date = date?.takeIf { readsAsDate(page, it) },
        startTime = startTime.keepIfMatches(page),
        endTime = endTime.keepIfMatches(page),
        ageMin = ageMin.keepIfMatches(page),
        contact = contact.keepIfMatches(page),
    ))
}

/**
 * Validates a location schema from the LM against the [document] it was made from, setting any other selector that
 * fails to null, wherever on the page its element sits.
 */
fun LocationSchema.validate(document: Element): Outcome<LocationSchema> {
    if (document.queryElement(name) == null) return Problem("Name selector does not match: $name")
    val page = listOf(document)
    return Ok(copy(
        description = description.keepIfMatches(page) { it.isPlausibleProse(allowsChrome = true) },
        address = address.keepIfMatches(page),
        phone = phone.keepIfMatches(page),
        email = email.keepIfMatches(page),
        hours = hours.keepIfMatches(page),
        eventsLink = eventsLink.keepIfMatches(page) { it.hasAttr("href") },
        image = image.keepIfMatches(page) { it.hasAttr("src") },
        socialLinks = socialLinks.keepIfMatches(page) { it.hasAttr("href") },
    ))
}

/**
 * Whether the text [selector] reads from these elements is, for at least half of them, a date: a month name, or
 * numbers such as 9/26.
 */
private fun readsAsDate(elements: List<Element>, selector: String): Boolean {
    val texts = elements.mapNotNull { it.queryElement(selector)?.text() }
    return texts.isNotEmpty() && texts.count { it.hasMonthName() || numericDate.containsMatchIn(it) } * 2 >= texts.size
}

private val numericDate = Regex("""\b\d{1,4}[/.-]\d{1,2}([/.-]\d{1,4})?\b""")

private fun String?.keepIfMatches(elements: List<Element>, test: (Element) -> Boolean = { true }): String? =
    this?.takeIf { selector -> elements.any { it.queryElement(selector, test) != null } }

/** The count of [events] each event-level selector of this schema matches, by field name. */
fun EventFeedSchema.fieldFill(events: List<Element>): Map<String, Int> = mapOf(
    "title" to title,
    "eventLocation" to eventLocation,
    "link" to link,
    "image" to image,
    "cost" to cost,
    "description" to description,
    "date" to date,
    "time" to time,
).mapNotNull { (name, selector) ->
    selector?.let { name to events.count { event -> event.queryElement(it) != null } }
}.toMap()

