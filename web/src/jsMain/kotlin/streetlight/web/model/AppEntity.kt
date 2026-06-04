package streetlight.web.model

import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.thumb
import koala.SiteImage
import koala.Svg
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.box
import koala.model.MapEntityId
import koala.model.MarkerUtility
import koala.model.PointEntity
import koala.model.Rgb
import kotlinx.html.DIV
import kotlinx.html.p
import streetlight.model.data.Galaxy
import streetlight.model.data.EventPost
import streetlight.model.data.Location
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritId
import streetlight.web.shells.cardOf
import streetlight.web.ui.eventFocusContent

interface AppEntity: PointEntity {
    val entityType: EntityType
}

enum class EntityType(label: String? = null): Labeled {
    Event,
    Location,
    Galaxy;

    override val label = label ?: name
}

data class LocationEntity(
    val location: Location,
): AppEntity {
    override val entityId get() = location.locationId.value.toString()
    // override val label get() = location.name
    override val geoPoint get() = location.geoPoint
    override val thumbUrl get() = location.images.thumb ?: SiteImage.placeholderTh.url
    override val focusContent: RenderContext.() -> Unit get() = {
        box {
            cardOf(location)
        }
    }
    override val light get() = Rgb(180, 240, 100)
    override val modifiers get() = modify(MarkerUtility.twinkleAboveRaincloud)
    override val entityType get() = EntityType.Location
}

data class EventEntity(
    val post: EventPost,
): AppEntity {
    override val entityId get() = post.event.locationId.value.toString()
    override val thumbUrl get() = post.images.thumb ?: SiteImage.placeholderTh.url
    override val light get() = Rgb(240, 100, 180 )
    override val focusContent: RenderContext.() -> Unit get() = {
        eventFocusContent(post)
    }
    override val geoPoint get() = post.event.geoPoint
    override val entityType get() = EntityType.Event
}

data class GalaxyEntity(
    val galaxy: Galaxy
): AppEntity {
    override val geoPoint get() = galaxy.geoPoint
    override val thumbUrl get() = galaxy.images.thumb
    override val entityId get() = galaxy.galaxyId.string
    override val entityType get() = EntityType.Galaxy
}