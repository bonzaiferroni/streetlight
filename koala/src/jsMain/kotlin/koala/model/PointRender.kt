package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Point
import koala.css.Focus
import koala.css.addModifiers
import koala.dom.modify
import koala.dom.onClick
import koala.dom.setProperty
import koala.dom.unmodify
import koala.external.MarkerOptions
import koala.external.maplibregl
import kotlinx.browser.document
import kotlinx.css.properties.s
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.p
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement

internal class PointRender(
    val jsMarker: maplibregl.Marker,
    marker: PointMarker,
    planarPoint: Point,
    val element: HTMLDivElement,
    val base: HTMLDivElement,
    val body: PointRenderBody,
) {
    var planarPoint = planarPoint
        private set
    var marker = marker
        private set

    var position = marker.geoPoint
        private set

    var isVisible = false
        private set

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
        this.position = position
    }

    private fun setOpacity(opacity: Float) {
        jsMarker.setOpacity(opacity.toString())
    }

    fun setIsVisible(value: Boolean, widget: maplibregl.Map) {
        if (isVisible == value) return
        isVisible = value
        if (isVisible) {
            jsMarker.addTo(widget)
        } else {
            jsMarker.remove()
        }
    }

    fun setClustering(isClusterPrincipal: Boolean?) {
        if (isClusterPrincipal == true) element.modify(MarkerStyle.ClusterPrincipal)
        else element.unmodify(MarkerStyle.ClusterPrincipal)
        if (isClusterPrincipal == false) element.modify(MarkerStyle.ClusterMember)
        else element.unmodify(MarkerStyle.ClusterMember)
    }

    fun unfocus() {
        element.unmodify(Focus)
    }

    fun focus() {
        element.modify(Focus)
    }

    fun dispose() {
        jsMarker.remove()
    }
}

internal fun PointMarker.toPointRender(pixelPoint: Point, focusEntity: () -> Unit): PointRender {
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
    var renderBody: PointRenderBody? = null

    element.append { // this element is modified by maplibre
        baseElement = div { // this element is all mine
            val delay = provideDelay()
            element.setProperty(MarkerStyle.TwinkleDelay.to(delay.s))
            element.setProperty(MarkerStyle.BodySize.to(bodySize))

            val baseMod = buildSet {
                add(MarkerStyle.Base)
                modifiers?.let {
                    addAll(it)
                }
                light?.let {
                    element.setProperty(MarkerStyle.MarkerLight.to(it))
                    add(MarkerStyle.MarkerGlow)
                }
                altitude?.let {
                    add(it)
                }
            }

            addModifiers(baseMod)

            renderBody = when (val marker = this@toPointRender) {
                is IconMarker -> configureIconRender(marker)
                is ThumbMarker -> configureThumbRender(marker)
                else -> error("unrecognized PointMarker")
            }
        }
    }

    val view = PointRender(
        jsMarker = jsMarker,
        marker = this,
        planarPoint = pixelPoint,
        element = element,
        base = baseElement!!,
        body = renderBody!!,
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