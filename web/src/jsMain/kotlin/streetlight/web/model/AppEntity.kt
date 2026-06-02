package streetlight.web.model

import kampfire.model.GeoPoint
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

data class LocationEntity(
    val location: Location,
): PointEntity {
    override val entityId get() = location.locationId.value.toString()
    // override val label get() = location.name
    override val position get() = location.geoPoint
    override val thumbUrl get() = location.images.thumb ?: SiteImage.placeholderTh.url
    override val focusContent: RenderContext.() -> Unit get() = {
        box {
            cardOf(location)
        }
    }
    override val light get() = Rgb(180, 240, 100)
    override val modifiers get() = modify(MarkerUtility.twinkleAboveRaincloud)
}

data class SpiritEntity(
    val spirit: Spirit
): PointEntity {
    override val entityId get() = spirit.spiritId.toEntityId()
    override val position get() = spirit.position
    override val body: DIV.() -> Unit get() = {
        p { +spirit.name }
    }
    override val light get() = Rgb(100, 180, 240)
}

fun SpiritId.toEntityId(): MapEntityId = "spirit-${value}"

data class IconEntity(
    override val entityId: MapEntityId,
    override val icon: Svg,
    override val position: GeoPoint
): PointEntity

data class EventEntity(
    val post: EventPost,
    val galaxy: Galaxy?,
    override val position: GeoPoint,
): PointEntity {
    override val entityId get() = post.event.locationId.value.toString()
    override val thumbUrl get() = post.images.thumb ?: galaxy?.images.thumb
        ?: SiteImage.placeholderTh.url
    override val light get() = Rgb(240, 100, 180 )
    override val focusContent: RenderContext.() -> Unit get() = {
        eventFocusContent(post)
    }
}