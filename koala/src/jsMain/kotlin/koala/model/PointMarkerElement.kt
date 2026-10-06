package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Point
import koala.modifier.*
import koala.dom.*
import koala.external.MarkerOptions
import koala.external.maplibregl
import kotlinx.css.properties.s
import kotlinx.html.js.div
import web.dom.document
import web.html.HTMLDivElement

internal class PointMarkerElement(
    val jsMarker: maplibregl.Marker,
    marker: PointMarker,
    planarPoint: Point,
    val element: HTMLDivElement,
    val base: HTMLDivElement,
    val body: PointMarkerBody,
    val light: LightHandle,
) {
    var planarPoint = planarPoint
        private set
    var marker = marker
        private set

    var position = marker.geoPoint
        private set

    var isVisible = false
        private set

    private var isClusterMember = false
    private var isAttached = false

    val isFocused get() = element.isModified(Focus)

    init {
        jsMarker.setLngLat(marker.geoPoint.toLngLat())
    }

    fun update(marker: PointMarker, planarPoint: Point) {
        this.marker = marker
        this.planarPoint = planarPoint
        move(marker.geoPoint)
        marker.opacity?.let {
            setOpacity(it)
        }
        body.update(marker)
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

    fun setIsVisible(value: Boolean, widget: maplibregl.Map) {
        if (isVisible == value) return
        isVisible = value
        updateAttachment(widget)
    }

    fun setCluster(cluster: PointCluster?, widget: maplibregl.Map) {
        isClusterMember = cluster != null && cluster.principalId != marker.markerId
        updateAttachment(widget)
    }

    /** Attaches the marker to [widget] while it is in view and not a cluster member, detaching it otherwise. */
    private fun updateAttachment(widget: maplibregl.Map) {
        val shouldAttach = isVisible && !isClusterMember
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

internal fun PointMarker.toPointRender(pixelPoint: Point, lightLayer: LightLayer, focusEntity: () -> Unit): PointMarkerElement {
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
    var renderBody: PointMarkerBody? = null
    val delay = provideDelay()

    element.append { // this element is modified by maplibre
        baseElement = div { // this element is all mine
            element.setStyle(MarkerStyle.TwinkleDelay.of(delay.s))
            element.setStyle(MarkerStyle.BodySize.of(bodySize))

            zIndex?.let {
                element.setStyle(Css.ZIndex.of(it))
            }

            val modifiers = modify(mod, MarkerStyle.Base, altitude?.cssClass)

            addModifiers(modifiers)

            renderBody = when (val marker = this@toPointRender) {
                is TravelMarker -> configureIconMarker(marker)
                is ThumbMarker -> configureThumbMarker(marker)
                is IconMarker -> configureIconMarker(marker)
                else -> error("unrecognized PointMarker")
            }
        }.asWeb()
    }

    val view = PointMarkerElement(
        jsMarker = jsMarker,
        marker = this,
        planarPoint = pixelPoint,
        element = element,
        base = baseElement!!,
        body = renderBody!!,
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