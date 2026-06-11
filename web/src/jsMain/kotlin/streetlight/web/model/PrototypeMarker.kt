package streetlight.web.model

import kampfire.model.GeoPoint
import koala.Svg
import koala.model.IconMarker
import koala.model.MarkerId
import koala.model.PointMarker
import kotlinx.css.LinearDimension
import kotlinx.css.px
import kotlinx.css.rgb
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritId

data class SpiritMarker(
    val spirit: Spirit,
): PointMarker {
    override val bodySize: LinearDimension get() = 24.px
    override val subpixelPositioning: Boolean get() = true

    override val markerId get() = spirit.spiritId.toEntityId()
    override val geoPoint get() = spirit.position
//    override val body: DIV.() -> Unit get() = {
//        p { +spirit.name }
//    }
    override val light get() = rgb(100, 180, 240)
}

fun SpiritId.toEntityId(): MarkerId = "spirit-${value}"

data class IconEntity(
    override val markerId: MarkerId,
    override val icon: Svg,
    override val geoPoint: GeoPoint
): IconMarker