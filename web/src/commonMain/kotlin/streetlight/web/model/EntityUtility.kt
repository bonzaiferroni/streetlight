package streetlight.web.model

import koala.SvgPack
import koala.SvgFile
import koala.model.WebRoute
import koala.model.Doc
import streetlight.model.data.EventGroup
import streetlight.model.data.City
import streetlight.model.data.CustomEntity
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.Offering
import streetlight.model.data.Skill
import streetlight.model.data.MediaPost
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.model.data.PostType
import streetlight.model.data.RecordType
import streetlight.model.data.Star
import streetlight.model.ui.CityRoute
import streetlight.model.ui.EventRoute
import streetlight.model.ui.GalaxyRoute
import streetlight.model.ui.LocationRoute
import streetlight.model.ui.MediaRoute
import streetlight.model.ui.OfferingRoute
import streetlight.model.ui.SkillRoute
import streetlight.model.ui.SiteDocRoute
import streetlight.model.ui.StarRoute
import streetlight.web.component.postMenu
import streetlight.web.component.starToggle
import streetlight.web.layout.EntityButton
import streetlight.web.layout.EntityDialog
import streetlight.web.layout.cellGrid
import koala.modifier.Koala
import koala.modifier.Rgb
import koala.modifier.mix
import streetlight.model.data.EntityType

val Location.route get() = LocationRoute(slug)
val Event.route get() = EventRoute(slug)
val EventLocation.eventRoute get() = EventRoute(eventSlug)
val EventLocation.locationRoute get() = LocationRoute(locationSlug)
val Doc.route get() = SiteDocRoute(docId)
val Media.route get() = MediaRoute(slug)
val Galaxy.route get() = GalaxyRoute(slug)

/** The page of this entity, the event's for an event at a location. */
fun Entity.toRoute(): WebRoute? = when (this) {
    is City -> CityRoute(slug)
    is EventLocation -> eventRoute
    is EventPost -> event.eventRoute
    is Event -> route
    is Galaxy -> route
    is LocationPost -> location.route
    is MediaPost -> media.route
    is Location -> route
    is Media -> route
    is CustomEntity -> route
    is Star -> StarRoute(username)
    is EventGroup -> null
    is Offering -> OfferingRoute(offeringId)
    is Skill -> SkillRoute(skillId)
}

/** The flair badge icon of this entity, the icon of its tag for an event that has one. */
fun Entity.toFlair(): SvgPack = when (this) {
    is EventLocation -> tag?.svg ?: FlairIcon.Event.svg
    is EventPost -> tag?.svg ?: FlairIcon.Event.svg
    is Event -> tag?.svg ?: FlairIcon.Event.svg
    is LocationPost, is Location -> FlairIcon.Location.svg
    is MediaPost, is Media -> FlairIcon.Media.svg
    is Skill -> tags?.firstOrNull()?.svg ?: SvgFile.Award
    is CustomEntity -> when (recordType) {
        RecordType.Location -> FlairIcon.Location.svg
        RecordType.Event -> FlairIcon.Event.svg
        else -> FlairIcon.Default.svg
    }
    else -> FlairIcon.Default.svg
}

/** The record whose whole entity the [EntityDialog] reads, or `null` for an entity it does not show. */
fun Entity.toEntityRef(): EntityRef? = when (this) {
    is EventLocation -> EntityRef(PostType.Event, eventSlug)
    is EventPost -> event.toEntityRef()
    is Event -> EntityRef(PostType.Event, slug)
    is LocationPost -> location.toEntityRef()
    is Location -> EntityRef(PostType.Location, slug)
    is MediaPost -> media.toEntityRef()
    is Media -> EntityRef(PostType.Media, slug)
    is City -> null
    is Galaxy -> null
    is CustomEntity -> null
    is Star -> null
    is EventGroup -> null
    is Offering -> null
    is Skill -> null
}

/** The buttons of this entity's [cellGrid]: its star toggle, and the post menu of a post. */
fun Entity.toButtons(): List<EntityButton>? = when (val entity = this) {
    is City -> null
    is EventLocation -> buildList {
        add(EntityButton { starToggle(entity) })
    }
    is EventPost -> entity.event.toButtons().orEmpty() + EntityButton {
        postMenu(entity.post.postId, entity.post.username)
    }
    is Event -> buildList {
        add(EntityButton { starToggle(entity) })
    }
    is Galaxy -> listOf(
        EntityButton { starToggle(entity) },
    )
    is LocationPost -> entity.location.toButtons().orEmpty() + EntityButton {
        postMenu(entity.post.postId, entity.post.username)
    }
    is MediaPost -> null
    is Location -> buildList {
        add(EntityButton { starToggle(entity) })
    }
    is Media -> null
    is CustomEntity -> null
    is Star -> null
    is EventGroup -> null
    is Offering -> null
    is Skill -> null
}

/** A second route shown under the title, such as the location of an event post. */
fun Entity.toSubRoute(): WebRoute? = when (this) {
    is MediaPost -> null
    is EventPost -> event.locationRoute
    is LocationPost -> null
    else -> null
}

fun Entity.toSubtitle(): String? = when (this) {
    is MediaPost -> media.subtitle
    is EventPost -> "${event.locationName}, ${event.city}"
    is LocationPost -> location.addressLine
    else -> null
}

/** The CSS value of this type's color, the custom property that holds it, or the primary color without a type. */
val EntityType?.cssValue get() = when (this) {
    null, EntityType.Media, EntityType.Star, EntityType.Offering, EntityType.Skill -> "var(--primary-fg)"
    EntityType.Event -> "var(--accent-fg)"
    EntityType.Galaxy -> "var(--sea-green-fg)"
    EntityType.Location -> "var(--green-fg)"
    EntityType.City -> "var(--yellow-fg)"
}

/** The color this type's color is mixed from, or the primary color without a type. */
val EntityType?.rgb: Rgb get() = when (this) {
    null, EntityType.Media, EntityType.Star, EntityType.Offering, EntityType.Skill -> Koala.primary
    EntityType.Event -> Koala.accent
    EntityType.Galaxy -> Koala.seaGreen
    EntityType.Location -> Koala.green
    EntityType.City -> Koala.yellow
}

/** The color of a marker's light: near white, with a hint of this type's color. */
val EntityType?.light get() = rgb.mix(Rgb.White, .8f)

val PostType.entityType get() = when (this) {
    PostType.Event -> EntityType.Event
    PostType.Location -> EntityType.Location
    PostType.Media -> EntityType.Media
}
