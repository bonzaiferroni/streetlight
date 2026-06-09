package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Svg
import koala.css.ModifierSet
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
    val bearing: Float? get() = null
    val opacity: Float? get() = null
    val subpixelPositioning: Boolean get() = true
    val icon: Svg? get() = null
    val thumbUrl: Url? get() = null
    val minZoom: Float? get() = null
    val modifiers: ModifierSet? get() = null
    val onFocus: OnFocus? get() = null
    val body: (DIV.() -> Unit)? get() = null
    val light: Rgb? get() = null
}

data class MarkerMovement(
    val markerId: MarkerId,
    val position: GeoPoint
)

data class Rgb(val r: Int, val g: Int, val b: Int) {
    fun css(): String = "${r.coerceIn(0, 255)}, ${g.coerceIn(0, 255)}, $${b.coerceIn(0, 255)}"
}

typealias OnFocus = (() -> Unit) -> Unit

interface LineMarker : GeoMarker {
    val lines: List<List<GeoPoint>>
    val color: String get() = "#4fd1c5"
    val width: Int get() = 2
    val joinShape: String get() = "round"
    val capShape: String get() = "round"
}