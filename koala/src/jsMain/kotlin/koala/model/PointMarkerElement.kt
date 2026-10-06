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

    var clusterSuperior: PointMarkerElement? = null
        private set
    private var clusterSuperiorDistanceSq = Double.POSITIVE_INFINITY

    var isClusterMember = false
        private set
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

    context(widget: maplibregl.Map)
    fun setIsVisible(value: Boolean) {
        if (isVisible == value) return
        isVisible = value
        updateAttachment()
    }

    /** Sets the nearest marker of higher priority, [clusterSuperiorDistanceSq] away in square meters. */
    fun setClusterSuperior(clusterSuperior: PointMarkerElement?, clusterSuperiorDistanceSq: Double) {
        this.clusterSuperior = clusterSuperior
        this.clusterSuperiorDistanceSq = clusterSuperiorDistanceSq
    }

    /** Clusters this marker into its [clusterSuperior] while it is within the cluster radius, [radiusSq] in square meters. */
    context(widget: maplibregl.Map)
    fun setClusterRadius(radiusSq: Double) {
        isClusterMember = clusterSuperiorDistanceSq <= radiusSq
        updateAttachment()
    }

    /** Finds the shown marker this one is clustered into, following [clusterSuperior] past the markers it is clustered with. */
    fun getClusterHead(): PointMarkerElement {
        var render = this
        while (render.isClusterMember) render = render.clusterSuperior ?: break
        return render
    }

    /** Attaches the marker to [widget] while it is in view and not a cluster member, detaching it otherwise. */
    context(widget: maplibregl.Map)
    private fun updateAttachment() {
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