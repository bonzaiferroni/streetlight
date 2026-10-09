package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Svg
import koala.modifier.*
import kotlinx.css.LinearDimension
import kotlinx.css.px
import kotlinx.html.DIV

typealias MarkerId = String

/** Something drawn on the map, identified by its [markerId]. */
sealed interface GeoMarker {
    val markerId: MarkerId
    val label: String? get() = null
}

/**
 * A marker at a single point, drawn as an element of [bodySize].
 *
 * [onFocus] wraps the focus of the marker when it is clicked. A marker with a [light] shows a light of that color in
 * the light layer beneath it, and one without shows none.
 */
interface PointMarker: GeoMarker {
    val geoPoint: GeoPoint
    val mod: Modifier? get() = null
    val onFocus: OnFocus? get() = null
    val light: Rgb? get() = null
    val opacity: Float? get() = null
    val zIndex: Int? get() = null

    val bodySize: LinearDimension
    val subpixelPositioning: Boolean get() = false
    val configureBody: DIV.() -> Unit
}

/**
 * A moving marker drawn as [icon], turned to its [bearing] in degrees clockwise from north.
 *
 * Its icon is drawn pointing up. [MarkerStyle.Bearing] in its [mod] turns it; a marker without it stays upright.
 */
interface TravelMarker: PointMarker {
    val icon: Svg
    val bearing: Float? get() = null
    override val mod: Modifier? get() = MarkerStyle.Bearing
    override val bodySize: LinearDimension get() = 24.px
    override val subpixelPositioning get() = true
    override val configureBody: DIV.() -> Unit get() = { configureTravelMarker(this@TravelMarker) }
}

/** A marker for an entity, with its type and theme color. */
interface StaticMarker: PointMarker {
    val typeLabel: String? get() = null
    val themeColor: String? get() = null
}

/** An [StaticMarker] drawn as a thumbnail with its label and sublabel. */
interface ThumbMarker: StaticMarker {
    val thumbUrl: Url
    val sublabel: String? get() = null
    override val bodySize: LinearDimension get() = 48.px
    override val configureBody: DIV.() -> Unit get() = { configureThumbMarker(this@ThumbMarker) }
}

/** An [StaticMarker] drawn as an icon with its label. */
interface IconMarker: StaticMarker {
    val svg: Svg
    override val bodySize: LinearDimension get() = 32.px
    override val configureBody: DIV.() -> Unit get() = { configureIconMarker(this@IconMarker) }
}

/** A marker moved to [position]. */
data class MarkerMovement(
    val markerId: MarkerId,
    val position: GeoPoint
)

/** Wraps the focus of a marker: it receives the function that focuses it, and decides when to call it. */
typealias OnFocus = (() -> Unit) -> Unit

/** Lines drawn on the map, with their color, width and shape. */
interface LineMarker : GeoMarker {
    val lines: List<List<GeoPoint>>
    val color: String get() = "#4fd1c5"
    val width: Int get() = 2
    val joinShape: String get() = "round"
    val capShape: String get() = "round"
}