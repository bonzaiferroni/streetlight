package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Point
import koala.modifier.*
import koala.dom.*
import koala.external.MarkerOptions
import koala.external.maplibregl
import kotlinx.css.properties.deg
import kotlinx.html.js.div
import web.dom.document
import web.html.HTMLDivElement

internal class PointMarkerElement(
    val jsMarker: maplibregl.Marker,
    marker: PointMarker,
    planarPoint: Point,
    val element: HTMLDivElement,
    val base: HTMLDivElement,
    val light: LightHandle,
) {
    var planarPoint = planarPoint
        private set
    var marker = marker
        private set

    var position = marker.geoPoint
        private set

    var isInBounds = false
        private set

    private var revealZoom = Float.NEGATIVE_INFINITY

    private var isRevealed = true
    private var isAttached = false
    private var turnedBearing = 0f

    val isFocused get() = element.isModified(Focus)

    init {
        jsMarker.setLngLat(marker.geoPoint.toLngLat())
        (marker as? TravelMarker)?.bearing?.let { setBearing(it) }
    }

    fun update(marker: PointMarker, planarPoint: Point) {
        this.marker = marker
        this.planarPoint = planarPoint
        move(marker.geoPoint)
        marker.opacity?.let {
            setOpacity(it)
        }
        (marker as? TravelMarker)?.bearing?.let { setBearing(it) }
    }

    /** Turns the marker to [bearing] the shorter way round, so its transition never spins the long way. */
    private fun setBearing(bearing: Float) {
        turnedBearing += (bearing - turnedBearing + 180).mod(360f) - 180
        base.setStyle(MarkerStyle.MarkerBearing.of(turnedBearing.deg))
    }

    private fun move(position: GeoPoint) {
        val current = jsMarker.getLngLat()
        val destination = position.toLngLat()
        val distance = current.distanceTo(destination)
        if (distance > 1) {
            jsMarker.move(current, destination)
        } else {
            jsMarker.setLngLat(destination)
        }
        light.move(position)
        this.position = position
    }

    private fun setOpacity(opacity: Float) {
        jsMarker.setOpacity(opacity.toString())
    }

    context(widget: maplibregl.Map)
    fun setIsInBounds(value: Boolean) {
        if (isInBounds == value) return
        isInBounds = value
        updateAttachment()
    }

    /** Sets the zoom from which this marker is shown rather than clustered. */
    fun setRevealZoom(revealZoom: Float) {
        this.revealZoom = revealZoom
    }

    /** Reveals this marker while [zoom] is at or above its reveal zoom. */
    context(widget: maplibregl.Map)
    fun setZoom(zoom: Float) {
        isRevealed = zoom >= revealZoom
        updateAttachment()
    }

    /** Attaches the marker to [widget] while it is in bounds and revealed, detaching it otherwise. */
    context(widget: maplibregl.Map)
    private fun updateAttachment() {
        val shouldAttach = isInBounds && isRevealed
        if (isAttached == shouldAttach) return
        isAttached = shouldAttach
        if (isAttached) {
            jsMarker.addTo(widget)
        } else {
            jsMarker.remove()
        }
    }

    fun unfocus() {
        element.unmodify(Focus)
    }

    fun focus() {
        element.modify(Focus)
    }

    fun dispose() {
        jsMarker.remove()
        light.dispose()
    }
}

internal fun PointMarker.toPointElement(planarPoint: Point, lightLayer: LightLayer, focusEntity: () -> Unit): PointMarkerElement {
    val element = document.createDiv()
    element.modify(MarkerStyle.Root)

    val options = MarkerOptions(
        element = element,
        subpixelPositioning = subpixelPositioning
    )

    val jsMarker = maplibregl.Marker(
        options = options
    )

    var baseElement: HTMLDivElement? = null
    val delay = provideDelay()

    element.append { // this element is modified by maplibre
        baseElement = div { // this element is all mine
            element.setStyle(MarkerStyle.BodySize.of(bodySize))

            zIndex?.let {
                element.setStyle(Css.ZIndex.of(it))
            }

            val modifiers = modify(mod, MarkerStyle.Base)

            addModifiers(modifiers)

            configureBody()
        }.asWeb()
    }

    val view = PointMarkerElement(
        jsMarker = jsMarker,
        marker = this,
        planarPoint = planarPoint,
        element = element,
        base = baseElement!!,
        light = lightLayer.allocate(geoPoint, -delay, 1f),
    )

    val onElementClick = onFocus?.let {
        { it(focusEntity) }
    } ?: focusEntity

    element.onClick {
        onElementClick()
    }

    return view
}

var twinkleIndex = 0

private fun provideDelay(): Float {
    return -(twinkleIndex++ % 24) * .2f
}