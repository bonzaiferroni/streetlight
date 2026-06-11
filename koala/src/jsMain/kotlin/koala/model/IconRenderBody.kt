package koala.model

import koala.css.addModifiers
import koala.css.modify
import koala.css.setStyle
import koala.dom.DOMContext
import koala.dom.setProperty
import kotlinx.css.properties.deg
import kotlinx.html.js.div
import kotlinx.html.js.p
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLParagraphElement

internal class IconRenderBody(
    override val element: HTMLElement,
    override val labelElement: HTMLElement?,
    val bearingElement: HTMLElement? = null,
    val bearing: Float? = null
): PointRenderBody {
    var lastBearing = 0f

    init {
        bearing?.let {
            setBearing(it)
        }
    }

    override fun update(marker: PointMarker) {
        val marker = marker as IconMarker
        marker.bearing?.let {
            setBearing(it)
        }
    }

    fun setBearing(bearing: Float) {
        val be = this.bearingElement ?: return
        val delta = ((bearing - lastBearing + 540) % 360) - 180;
        lastBearing += delta
        val adjusted = lastBearing - 90
        be.setProperty(MarkerStyle.MarkerBearing.to(adjusted.deg))
    }
}

internal fun DOMContext.configureIconRender(marker: IconMarker): IconRenderBody {
    with(marker) {
        val body = div {
            addModifiers(modify(MarkerStyle.Icon, MarkerStyle.Body))
            setStyle(MarkerStyle.MarkerSvg.to(marker.icon))
        }

        val bearingElement = marker.bearing?.let {
            div {
                addModifiers(MarkerStyle.Bearing)
            }
        }

        val labelElement = label?.let {
            p {
                addModifiers(MarkerStyle.Label)
                +it
            }
        }

        return IconRenderBody(body, labelElement, bearingElement, marker.bearing)
    }
}