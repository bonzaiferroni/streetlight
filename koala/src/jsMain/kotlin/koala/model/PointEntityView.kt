package koala.model

import kampfire.model.GeoPoint
import kampfire.model.Point
import koala.css.Css
import koala.css.Focus
import koala.css.Scale
import koala.css.applyModifiers
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

    var position = entity.position
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
    val block = Css("map-marker")
    val base = Css("map-marker__base")
    val bearing = Css("map-marker__bearing")
    val icon = Css("map-marker__icon")
    val thumb = Css("map-marker__thumb")
    val body = Css("map-marker__body")
    val label = Css("map-marker__label")
}

object MarkerUtility {
    val twinkleAboveKite = Css("twinkle-above-kite")
    val twinkleAboveRaincloud = Css("twinkle-above-raincloud")
    val twinkleAboveAirplane = Css("twinkle-above-airplane")
}

fun PointEntity.toMapEntityView(pixelPoint: Point): PointEntityView {
    val element = document.createDiv()
    element.modify(MarkerCss.block)

    var baseElement: HTMLDivElement? = null
    var bearingElement: HTMLDivElement? = null
    var bodyElement: HTMLElement? = null
    var labelElement: HTMLParagraphElement? = null

    element.append {
        baseElement = div {
            val baseModifiers = modify(MarkerCss.base).let { set ->
                modifiers?.let { set + it } ?: set
            }.let { set ->
                light?.let {
                    style = "--light: ${it.css()};"
                    set + Css("marker-glow")
                } ?: set
            }
            applyModifiers(baseModifiers)

            bearingElement = bearing?.let {
                div {
                    applyModifiers(MarkerCss.bearing)
                }
            }

            bodyElement = iconPath?.let {
                div {
                    applyModifiers(modify(MarkerCss.icon, MarkerCss.body))
                    style = "--svg: url(${iconPath});"
                }
            } ?: thumbPath?.let {
                img {
                    src = it
                    applyModifiers(modify(MarkerCss.body, MarkerCss.thumb))
                }
            } ?: body?.let {
                div {
                    applyModifiers(modify(MarkerCss.body))
                    body?.invoke(this)
                }
            }

            labelElement = label?.let {
                p {
                    applyModifiers(MarkerCss.label)
                    +it
                }
            }
        }
    }

    if (thumbPath != null) baseElement?.modify(Scale)

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
    onClick?.let {
        element.onClick(it)
    }

    return view
}