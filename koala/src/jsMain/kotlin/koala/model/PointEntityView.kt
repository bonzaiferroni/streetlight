package koala.model

import kampfire.model.Point
import koala.css.Css
import koala.css.applyModifiers
import koala.dom.modify
import koala.dom.onClick
import koala.dom.unmodify
import koala.external.MarkerOptions
import koala.external.maplibregl
import koala.html.GeoMapSelector
import kotlinx.browser.document
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.img
import kotlinx.html.style
import org.w3c.dom.HTMLElement

class PointEntityView(
    val marker: maplibregl.Marker,
    entity: PointEntity,
    pixelPoint: Point,
    val base: HTMLElement?,
    val body: HTMLElement?,
    val bearing: HTMLElement? = null,
) {
    var lastBearing = 0f

    var point = pixelPoint
        private set
    var entity = entity
        private set

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
        base?.unmodify(GeoMapSelector.focused)
    }

    fun focus() {
        base?.modify(GeoMapSelector.focused)
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

object MarkerClass {
    val base = Css("map-marker")
    val bearing = Css("marker-bearing")
    val icon = Css("marker-icon")
    val thumb = Css("marker-thumb")
}

fun PointEntity.toMapEntityView(pixelPoint: Point): PointEntityView {
    val element = document.createDiv().also {
        it.modify(MarkerClass.base)
    }

    var bearingElement: HTMLElement? = null
    var bodyElement: HTMLElement? = null

    element.append {
        bearingElement = bearing?.let {
            div {
                applyModifiers(MarkerClass.bearing)
            }
        }
        bodyElement = iconPath?.let {
            div {
                applyModifiers(MarkerClass.icon)
                style = "--svg: url(${iconPath});"
            }
        } ?: thumbPath?.let {
            img {
                src = it
                applyModifiers(MarkerClass.thumb)
            }
        }
    }

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
        base = element,
        body = bodyElement,
        bearing = bearingElement
    )
    onClick?.let {
        element.onClick(it)
    }

    return view
}