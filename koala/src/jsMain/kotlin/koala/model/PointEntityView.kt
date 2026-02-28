package koala.model

import kampfire.model.Point
import koala.css.Css
import koala.dom.modify
import koala.dom.onClick
import koala.dom.unmodify
import koala.external.MarkerOptions
import koala.external.maplibregl
import koala.html.GeoMapSelector
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

class PointEntityView(
    val marker: maplibregl.Marker,
    val entity: PointEntity,
    pixelPoint: Point,
    val base: HTMLElement?,
    val body: HTMLElement?,
    val bearing: HTMLElement? = null,
) {
    var lastBearing = 0f

    var point = pixelPoint
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

    fun setPixelPoint(point: Point) {
        this@PointEntityView.point = point
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

    val bearingElement = bearing?.let { _ ->
        document.createDiv().also {
            it.modify(MarkerClass.bearing)
            element.appendChild(it)
        }
    }

    val bodyElement = iconPath?.let { iconPath ->
        document.createDiv().also {
            it.style.setProperty("--svg", "url(${iconPath})")
            it.modify(MarkerClass.icon)
            element.appendChild(it)
        }
    } ?: thumbPath?.let { thumbPath ->
        document.createImg(thumbPath).also {
            it.modify(MarkerClass.thumb)
            element.appendChild(it)
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