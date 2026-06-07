package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Point
import koala.css.Class
import koala.css.Focus
import koala.css.Scale
import koala.css.addModifiers
import koala.css.modify
import koala.dom.modify
import koala.dom.onClick
import koala.dom.unmodify
import koala.external.MarkerOptions
import koala.external.maplibregl
import koala.html.MarkerElement
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.js.p
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement

class MarkerElement(
    val jsMarker: maplibregl.Marker,
    marker: PointMarker,
    pixelPoint: Point,
    val element: HTMLDivElement?,
    val base: HTMLDivElement?,
    val body: HTMLElement?,
    val label: HTMLElement?,
    val bearing: HTMLElement? = null,
) {
    var lastBearing = 0f

    var point = pixelPoint
        private set
    var marker = marker
        private set

    var position = marker.geoPoint
        private set

    var isVisible = false
        private set

    fun move(position: GeoPoint) {
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

    fun setBearing(bearing: Float) {
        val be = this@MarkerElement.bearing ?: return
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        be.style.setProperty("--bearing", "${adjusted}deg")
    }

    fun setOpacity(opacity: Float) {
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

    fun setEntity(entity: PointMarker, point: Point) {
        this.marker = entity
        this.point = point
    }

    fun unfocus() {
        element?.unmodify(Focus)
    }

    fun focus() {
        element?.modify(Focus)
    }
}

fun koala.model.MarkerElement.setAttributes(entity: PointMarker) {
    entity.bearing?.let {
        setBearing(it)
    }
    entity.opacity?.let {
        setOpacity(it)
    }
}

fun PointMarker.toMarkerView(pixelPoint: Point, focusEntity: () -> Unit): koala.model.MarkerElement {
    val element = document.createDiv()
    element.modify(MarkerElement.Class)

    var baseElement: HTMLDivElement? = null
    var bearingElement: HTMLDivElement? = null
    var bodyElement: HTMLElement? = null
    var labelElement: HTMLParagraphElement? = null

    element.append {
        baseElement = div {
            val delay = provideDelay()
            element.style.setProperty("--twinkle-delay", "${delay}s")

            val baseModifiers = modify(MarkerElement.Base).let { set ->
                modifiers?.let { set + it } ?: set
            }.let { set ->
                light?.let {
                    style = "--light: ${it.css()};"
                    set + Class("marker-glow")
                } ?: set
            }
            addModifiers(baseModifiers)

            bearingElement = bearing?.let {
                div {
                    addModifiers(MarkerElement.Bearing)
                }
            }

            bodyElement = icon?.let {
                div {
                    addModifiers(modify(MarkerElement.Icon, MarkerElement.Body))
                    style = "--svg: url(${it.url});"
                }
            } ?: thumbUrl?.let {
                img {
                    src = it.value
                    addModifiers(modify(MarkerElement.Body, MarkerElement.Thumb))
                }
            } ?: body?.let {
                div {
                    addModifiers(modify(MarkerElement.Body))
                    body?.invoke(this)
                }
            }

            labelElement = label?.let {
                p {
                    addModifiers(MarkerElement.Label)
                    +it
                }
            }
        }
    }

    if (thumbUrl != null) baseElement?.modify(Scale)

    val options = MarkerOptions(
        element = element,
        subpixelPositioning = subpixelPositioning
    )
    val view = MarkerElement(
        jsMarker = maplibregl.Marker(
            options = options
        ),
        marker = this,
        pixelPoint = pixelPoint,
        element = element,
        base = baseElement,
        body = bodyElement,
        label = labelElement,
        bearing = bearingElement,
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