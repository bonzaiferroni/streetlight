package streetlight.web.model

import kampfire.model.GeoPoint
import koala.Svg
import koala.model.MarkerId
import koala.model.PointMarker
import koala.model.Rgb
import kotlinx.html.DIV
import kotlinx.html.p
import streetlight.model.data.Spirit
import streetlight.model.data.SpiritId

data class SpiritEntity(
    val spirit: Spirit
): PointMarker {
    override val markerId get() = spirit.spiritId.toEntityId()
    override val geoPoint get() = spirit.position
    override val body: DIV.() -> Unit get() = {
        p { +spirit.name }
    }
    override val light get() = Rgb(100, 180, 240)
}

fun SpiritId.toEntityId(): MarkerId = "spirit-${value}"

data class IconEntity(
    override val markerId: MarkerId,
    override val icon: Svg,
    override val geoPoint: GeoPoint
): PointMarker