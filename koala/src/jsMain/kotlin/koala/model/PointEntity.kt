package koala.model

import kampfire.model.GeoPoint
import koala.css.Css
import koala.dom.modify
import koala.dom.onClick
import koala.external.maplibregl
import kotlinx.browser.document
import org.w3c.dom.HTMLElement

interface PointEntity: MapEntity {
    val position: GeoPoint
    val bearing: Float? get() = null
    val opacity: Float? get() = null
    val subpixelPositioning: Boolean get() = true
    val iconPath: String? get() = null
    val thumbPath: String? get() = null
    val minZoom: Float? get() = null
    val onClick: (() -> Unit)? get() = null
}

data class MapObject(
    val marker: maplibregl.Marker,
    val entity: PointEntity,
    val base: HTMLElement?,
    val body: HTMLElement?,
    val bearing: HTMLElement? = null,
) {
    var lastBearing = 0f

    fun setBearing(bearing: Float) {
        val be = this@MapObject.bearing ?: return
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        be.style.setProperty("--bearing", "${adjusted}deg")
    }

    fun setOpacity(opacity: Float) {
        marker.setOpacity(opacity.toString())
    }
}

fun MapContext.recallObject(entity: PointEntity): MapObject? {
    val markerElement = markers[entity.entityId] ?: return null
    val current = markerElement.marker.getLngLat()
    val destination = entity.position.toLngLat()
    val distance = current.distanceTo(destination)
    if (distance > 1) {
        markerElement.marker.move(current, destination)
    } else {
        markerElement.marker.setLngLat(destination)
    }
    return markerElement
}

fun MapContext.createObject(entity: PointEntity): MapObject {
    val element = document.createDiv().also {
        it.modify(MarkerClass.base)
    }

    val bearingElement = entity.bearing?.let { _ ->
        document.createDiv().also {
            it.modify(MarkerClass.bearing)
            element.appendChild(it)
        }
    }

    val bodyElement = entity.iconPath?.let { iconPath ->
        document.createDiv().also {
            it.style.setProperty("--svg", "url(${iconPath})")
            it.modify(MarkerClass.icon)
            element.appendChild(it)
        }
    } ?: entity.thumbPath?.let { thumbPath ->
        document.createImg(thumbPath).also {
            it.modify(MarkerClass.thumb)
            element.appendChild(it)
        }
    }

    val options = jsObject {
        this.element = element
        subpixelPositioning = entity.subpixelPositioning
    }
    val mapObject = MapObject(
        marker = maplibregl.Marker(
            options = options
        ),
        entity = entity,
        base = element,
        body = bodyElement,
        bearing = bearingElement
    )
    val onClick = entity.onClick
    if (onClick != null) {
        element.onClick(onClick)
    }
    mapObject.marker.setLngLat(entity.position.toLngLat())
    mapObject.marker.addTo(widget)
    markers[entity.entityId] = mapObject
    return mapObject
}

fun MapObject.setAttributes(entity: PointEntity) {
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