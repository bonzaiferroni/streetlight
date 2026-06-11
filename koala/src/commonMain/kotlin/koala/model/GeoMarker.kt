package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Svg
import koala.css.ModifierSet
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.px
import kotlinx.html.DIV

typealias MarkerId = String

sealed interface GeoMarker {
    val markerId: MarkerId
    val label: String? get() = null
    val altitude: Altitude? get() = null
}

data class PanPoint(
    val point: GeoPoint,
    val zoom: Float? = null,
    val snap: Boolean = false,
)

interface PointMarker: GeoMarker {
    val geoPoint: GeoPoint
    val modifiers: ModifierSet? get() = null
    val onFocus: OnFocus? get() = null
    val light: Color? get() = null
    val opacity: Float? get() = null

    val bodySize: LinearDimension
    val subpixelPositioning: Boolean
}

interface IconMarker: PointMarker {
    val icon: Svg
    val bearing: Float? get() = null
    override val bodySize: LinearDimension get() = 24.px
    override val subpixelPositioning get() = true
}

interface ThumbMarker: PointMarker {
    val thumbUrl: Url
    override val bodySize: LinearDimension get() = 48.px
    override val subpixelPositioning get() = false
}

data class MarkerMovement(
    val markerId: MarkerId,
    val position: GeoPoint
)

typealias OnFocus = (() -> Unit) -> Unit

interface LineMarker : GeoMarker {
    val lines: List<List<GeoPoint>>
    val color: String get() = "#4fd1c5"
    val width: Int get() = 2
    val joinShape: String get() = "round"
    val capShape: String get() = "round"
}