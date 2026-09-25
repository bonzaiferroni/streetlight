package streetlight.server.daemon

import com.fleeksoft.ksoup.nodes.Element
import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import kampfire.model.toDataOr
import streetlight.agent.tryQuery
import streetlight.model.data.EventFeedSchema
import streetlight.model.data.EventPageSchema

object SchemaProblem {
    val Invalid = Problem("The LM schema failed validation against its page.")
}

/**
 * Runs each selector of a feed schema from the LM against the [body] it was made from.
 *
 * The event selector must match at least one element. Any other selector that is invalid, or matches
 * in no event, is set to null.
 */
fun EventFeedSchema.validate(body: Element): Outcome<EventFeedSchema> {
    val selector = event ?: return Problem("No event selector")
    val events = body.tryQuery(selector).toDataOr { return it }
    if (events.isEmpty()) return Problem("Event selector matched nothing: $selector")
    val page = listOf(body)
    return Ok(copy(
        feedLocation = feedLocation.keepIfMatches(page),
        address = address.keepIfMatches(page),
        title = title.keepIfMatches(events),
        eventLocation = eventLocation.keepIfMatches(events),
        link = link.keepIfMatches(events),
        image = image.keepIfMatches(events),
        cost = cost.keepIfMatches(events),
        description = description.keepIfMatches(events),
        date = date.keepIfMatches(events),
        time = time.keepIfMatches(events),
    ))
}

/**
 * Runs each selector of a page schema from the LM against the [body] it was made from.
 *
 * The title and description must pass the same test a stored schema must pass to be reused. Any other
 * selector that is invalid, or matches nothing, is set to null.
 */
fun EventPageSchema.validate(body: Element): Outcome<EventPageSchema> {
    if (!body.queryElement(title).isPlausibleField()) return Problem("Title selector does not match: $title")
    if (!body.queryElement(description).isPlausibleProse()) {
        return Problem("Description selector does not match: $description")
    }
    val page = listOf(body)
    return Ok(copy(
        location = location.keepIfMatches(page),
        address = address.keepIfMatches(page),
        image = image.keepIfMatches(page),
        cost = cost.keepIfMatches(page),
        date = date.keepIfMatches(page),
        startTime = startTime.keepIfMatches(page),
        endTime = endTime.keepIfMatches(page),
        ageMin = ageMin.keepIfMatches(page),
        contact = contact.keepIfMatches(page),
    ))
}

private fun String?.keepIfMatches(elements: List<Element>): String? =
    this?.takeIf { selector -> elements.any { it.queryElement(selector) != null } }

/** How many of [events] each event-level selector of this schema matches, by field name. */
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

