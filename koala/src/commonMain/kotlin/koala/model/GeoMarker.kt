package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Url
import koala.Svg
import koala.modifier.*
import kotlinx.css.Color
import kotlinx.css.LinearDimension
import kotlinx.css.px

typealias MarkerId = String

/** Something drawn on the map, identified by its [markerId]. */
sealed interface GeoMarker {
    val markerId: MarkerId
    val label: String? get() = null
    val altitude: Altitude? get() = null
}

/**
 * A marker at a single point, drawn as an element of [bodySize].
 *
 * [onFocus] wraps the focus of the marker when it is clicked, and [light] adds a glow of that color.
 */
interface PointMarker: GeoMarker {
    val geoPoint: GeoPoint
    val mod: Modifier? get() = null
    val onFocus: OnFocus? get() = null
    val light: Color? get() = null
    val opacity: Float? get() = null
    val zIndex: Int? get() = null

    val bodySize: LinearDimension
    val subpixelPositioning: Boolean get() = false
}

/** A moving marker drawn as [icon], turned to its [bearing]. */
interface TravelMarker: PointMarker {
    val icon: Svg
    val bearing: Float? get() = null
    override val bodySize: LinearDimension get() = 24.px
    override val subpixelPositioning get() = true
}

/** A marker for an entity, with its type and theme color. */
interface EntityMarker: PointMarker {
    val typeLabel: String? get() = null
    val themeColor: String? get() = null
}

/** An [EntityMarker] drawn as a thumbnail with its label and sublabel. */
interface ThumbMarker: EntityMarker {
    val thumbUrl: Url
    val sublabel: String? get() = null
    override val bodySize: LinearDimension get() = 48.px
}

/** An [EntityMarker] drawn as an icon with its label. */
interface IconMarker: EntityMarker {
    val svg: Svg
    override val bodySize: LinearDimension get() = 32.px
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