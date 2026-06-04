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
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.js.p
import kotlinx.html.style
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement

class PointEntityView(
    val marker: maplibregl.Marker,
    entity: PointEntity,
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
    var entity = entity
        private set

    var position = entity.geoPoint
        private set

    var isVisible = false
        private set

    fun move(position: GeoPoint) {
        val current = marker.getLngLat()
        val destination = position.toLngLat()
        val distance = current.distanceTo(destination)
        if (distance > 1) {
            marker.move(current, destination)
        } else {
            marker.setLngLat(destination)
        }
        this.position = position
    }

    fun setBearing(bearing: Float) {
        val be = this@PointEntityView.bearing ?: return
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        be.style.setProperty("--bearing", "${adjusted}deg")
    }

    fun setOpacity(opacity: Float) {
        marker.setOpacity(opacity.toString())
    }

    fun setIsVisible(value: Boolean, widget: maplibregl.Map) {
        if (isVisible == value) return
        isVisible = value
        if (isVisible) {
            marker.addTo(widget)
        } else {
            marker.remove()
        }
    }

    fun setEntity(entity: PointEntity, point: Point) {
        this.entity = entity
        this.point = point
    }

    fun unfocus() {
        element?.unmodify(Focus)
    }

    fun focus() {
        element?.modify(Focus)
    }
}

fun PointEntityView.setAttributes(entity: PointEntity) {
    entity.bearing?.let {
        setBearing(it)
    }
    entity.opacity?.let {
        setOpacity(it)
    }
}

object MarkerCss {
    val block = Class("map-marker")
    val base = Class("map-marker__base")
    val bearing = Class("map-marker__bearing")
    val icon = Class("map-marker__icon")
    val thumb = Class("map-marker__thumb")
    val body = Class("map-marker__body")
    val label = Class("map-marker__label")
}

object MarkerUtility {
    val twinkleAboveKite = Class("twinkle-above-kite")
    val twinkleAboveRaincloud = Class("twinkle-above-raincloud")
    val twinkleAboveAirplane = Class("twinkle-above-airplane")
}

fun PointEntity.toMapEntityView(pixelPoint: Point, focusEntity: () -> Unit): PointEntityView {
    val element = document.createDiv()
    element.modify(MarkerCss.block)

    var baseElement: HTMLDivElement? = null
    var bearingElement: HTMLDivElement? = null
    var bodyElement: HTMLElement? = null
    var labelElement: HTMLParagraphElement? = null

    element.append {
        baseElement = div {
            val delay = provideDelay()
            element.style.setProperty("--twinkle-delay", "${delay}s")

            val baseModifiers = modify(MarkerCss.base).let { set ->
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
                    addModifiers(MarkerCss.bearing)
                }
            }

            bodyElement = icon?.let {
                div {
                    addModifiers(modify(MarkerCss.icon, MarkerCss.body))
                    style = "--svg: url(${it.url});"
                }
            } ?: thumbUrl?.let {
                img {
                    src = it.value
                    addModifiers(modify(MarkerCss.body, MarkerCss.thumb))
                }
            } ?: body?.let {
                div {
                    addModifiers(modify(MarkerCss.body))
                    body?.invoke(this)
                }
            }

            labelElement = label?.let {
                p {
                    addModifiers(MarkerCss.label)
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
    val view = PointEntityView(
        marker = maplibregl.Marker(
            options = options
        ),
        entity = this,
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