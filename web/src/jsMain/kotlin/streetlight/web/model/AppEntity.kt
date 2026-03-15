package streetlight.web.model

import kampfire.model.GeoPoint
import koala.css.MaxWidth64
import koala.css.modify
import koala.dom.RenderContext
import koala.dom.box
import koala.html.SiteImage
import koala.model.MapContextId
import koala.model.MapEntityId
import koala.model.MarkerUtility
import koala.model.PointEntity
import koala.model.Rgb
import kotlinx.html.DIV
import kotlinx.html.p
import streetlight.model.data.Event
import streetlight.model.data.Galaxy
import streetlight.model.data.GalaxyPost
import streetlight.model.data.Location
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritId
import streetlight.web.shells.cardOf

data class LocationEntity(
    val location: Location,
): PointEntity {
    override val entityId get() = location.locationId.value
    // override val label get() = location.name
    override val position get() = location.geoPoint
    override val thumbPath get() = location.thumbUrl ?: SiteImage.placeholderThumb
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(location)
        }
    }
    override val light get() = Rgb(180, 240, 100)
    override val modifiers get() = modify(MarkerUtility.twinkleAboveRaincloud)
}

data class EventEntity(
    val location: Location,
    val events: List<Event>,
): PointEntity {
    val event get() = events.first()
    override val entityId get() = location.locationId.value
    // override val label get() = location.name
    override val position get() = location.geoPoint
    override val thumbPath get() = event.thumbUrl ?: SiteImage.placeholderThumb
    override val focusCard: RenderContext.() -> Unit get() = {
        box {
            cardOf(event, modify(MaxWidth64))
        }
    }
    override val light get() = Rgb(240, 100, 180 )
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
    override val iconPath: String,
    override val position: GeoPoint
): PointEntity

data class PostEntity(
    val post: GalaxyPost,
    val galaxy: Galaxy,
    override val position: GeoPoint,
): PointEntity {
    override val entityId get() = post.location?.locationId?.value ?: post.postId.value
    override val thumbPath get() = post.event?.thumbUrl ?: post.location?.thumbUrl ?: galaxy.thumbUrl
        ?: SiteImage.placeholderThumb
    override val light get() = Rgb(240, 100, 180 )
}