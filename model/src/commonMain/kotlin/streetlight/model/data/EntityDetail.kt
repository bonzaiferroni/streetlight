package streetlight.model.data

import kabinet.utils.format
import kabinet.utils.toAgoFormat
import kabinet.utils.toFutureFormat
import kabinet.utils.toMetricString
import kabinet.utils.toTimeFormat
import kampfire.model.Url
import koala.Svg
import koala.SvgFile
import koala.model.WebRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.StarRoute
import kotlin.time.Instant

/**
 * A fact shown with an entity: its [text] after its [svg], or after its [label] when it has no icon, linking to the
 * page of [route] or to [url] when given, and placed first when [isPriority]. A fact of the entity itself takes the scheme color of where it is
 * shown, and a fact drawn from another entity, such as an event's location, names that entity's [type] for its color.
 */
data class EntityDetail(
    val text: String,
    val svg: Svg? = null,
    val label: String? = null,
    val type: EntityType? = null,
    val url: Url? = null,
    val route: WebRoute? = null,
    val isPriority: Boolean = false,
)

/**
 * The details of this entity that are known, the priority ones first: when it happens or what kind of thing it is, then
 * where it is and what it costs or holds. Shown in the feed of a location's own page, as [context] says, an event
 * leaves out its location.
 */
fun Entity.toDetails(context: FeedContext? = null): List<EntityDetail> = when (this) {
    is City -> listOfNotNull(
        state?.let { EntityDetail(it, SvgFile.Map.small, isPriority = true) },
    ) + countDetailsOf(locationCount, eventCount)
    is EventLocation -> eventDetailsOf(
        startsAt, cost, url, tag,
        locationName = locationLabel.takeIf { context !is FeedContext.Location },
        locationRoute = LocationRoute(locationSlug),
    )
    is EventPost -> event.toDetails(context)
    is Event -> eventDetailsOf(startsAt, cost, url, tag, null)
    is Galaxy -> listOfNotNull(
        tagline?.let { EntityDetail(it, SvgFile.Planet.small, isPriority = true) },
    ) + countDetailsOf(locationCount, eventCount)
    is LocationPost -> location.toDetails(context)
    is Location -> locationDetailsOf(mapType, city, eventCount)
    is MediaPost -> media.toDetails(context)
    is Media -> listOfNotNull(
        subtitle?.let { EntityDetail(it, SvgFile.Quote.small, isPriority = true) },
        EntityDetail(username.value, SvgFile.User.small),
    )
    is CustomEntity -> listOfNotNull(
        sublabel?.let { EntityDetail(it, SvgFile.Tag.small, isPriority = true) },
    )
    is Star -> listOfNotNull(
        tagline?.let { EntityDetail(it, SvgFile.Star.small, isPriority = true) },
        EntityDetail(createdAt.toAgoFormat(), SvgFile.UserPlus.small),
        skillCount.takeIf { it > 0 }?.let {
            EntityDetail("${it.toMetricString()} skills", SvgFile.Award.small, route = StarRoute(username))
        },
    )
    is EventGroup -> listOfNotNull(
        startsAt?.let { EntityDetail(it.toFutureFormat(), SvgFile.Calendar.small, isPriority = true) },
        startsAt?.let { EntityDetail(it.toTimeFormat(), SvgFile.Clock.small, isPriority = true) },
        locationName?.let { EntityDetail(it, SvgFile.MapPin.small, type = EntityType.Location) },
        eventCountDetailOf(eventCount),
    )
    is Offering -> listOf(EntityDetail(priceLabel, SvgFile.Coin.small))
    is Skill -> listOf(
        EntityDetail(skillLevel.label, SvgFile.Award.small, isPriority = true),
        EntityDetail("since $yearStarted", SvgFile.Calendar.small),
    )
}

/**
 * The details of an event that are known: its day and time, its cost linking to [purchaseUrl] when it is not free, its
 * [tag], and [locationName] linking to [locationRoute].
 */
fun eventDetailsOf(
    startsAt: Instant?,
    cost: Float?,
    purchaseUrl: Url?,
    tag: EventTag?,
    locationName: String?,
    locationRoute: WebRoute? = null,
) = listOfNotNull(
    startsAt?.let { EntityDetail(it.toFutureFormat(), SvgFile.Calendar.small, isPriority = true) },
    startsAt?.let { EntityDetail(it.toTimeFormat(), SvgFile.Clock.small, isPriority = true) },
    cost?.let { costDetailOf(it, purchaseUrl) },
    tag?.let { EntityDetail(it.label, SvgFile.Label.small) },
    locationName?.let { EntityDetail(it, SvgFile.MapPin.small, type = EntityType.Location, route = locationRoute) },
)

/** The details of a location that are known: its [mapType], its [city], and its [eventCount] when it has events. */
fun locationDetailsOf(mapType: String?, city: String?, eventCount: Int) = listOfNotNull(
    EntityDetail(mapType ?: "Location", SvgFile.MapPin.small, isPriority = true),
    city?.let { EntityDetail(it, SvgFile.City.small) },
    eventCountDetailOf(eventCount),
)

/** The cost, or FREE, linking to [purchaseUrl] when there is a cost. */
fun costDetailOf(cost: Float, purchaseUrl: Url?) = EntityDetail(
    text = if (cost == 0f) "FREE" else "$${cost.format(2, true)}",
    svg = SvgFile.Ticket.small,
    url = purchaseUrl.takeIf { cost != 0f },
)

/** The location and event counts of a place, each when it has any. */
private fun countDetailsOf(locationCount: Int, eventCount: Int) = listOfNotNull(
    locationCount.takeIf { it > 0 }?.let { EntityDetail("${it.toMetricString()} locations", SvgFile.MapPin.small) },
    eventCountDetailOf(eventCount),
)

private fun eventCountDetailOf(eventCount: Int) =
    eventCount.takeIf { it > 0 }?.let { EntityDetail("${it.toMetricString()} events", SvgFile.Calendar.small) }
