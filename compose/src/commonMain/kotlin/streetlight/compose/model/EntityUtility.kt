package streetlight.compose.model

import koala.SvgFile
import koala.SvgPack
import streetlight.model.data.CustomEntity
import streetlight.model.data.Entity
import streetlight.model.data.Event
import streetlight.model.data.EventLocation
import streetlight.model.data.EventPost
import streetlight.model.data.Galaxy
import streetlight.model.data.Location
import streetlight.model.data.LocationPost
import streetlight.model.data.Media
import streetlight.model.data.MediaPost
import streetlight.model.data.RecordType
import streetlight.model.data.Skill

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

/** The route of this entity's page, the event's for an event at a location, or `null` while the app has no page for it. */
fun Entity.toRoute(): AppRoute? = when (this) {
    is EventLocation -> EventRoute(eventSlug)
    is EventPost -> event.toRoute()
    is Event -> EventRoute(slug)
    is Galaxy -> GalaxyRoute(slug)
    else -> null
}
