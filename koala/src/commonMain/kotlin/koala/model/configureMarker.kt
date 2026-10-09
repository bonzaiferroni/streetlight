package koala.model

import koala.html.box
import koala.html.column
import koala.html.icon
import koala.html.row
import koala.html.span
import koala.html.textBlock
import koala.modifier.AlignItemsCenter
import koala.modifier.Bold
import koala.modifier.ColorSchemeFg
import koala.modifier.Css
import koala.modifier.Gap0
import koala.modifier.Gap2Px
import koala.modifier.LineHeight115
import koala.modifier.OpacityHalf
import koala.modifier.OpacityHigh
import koala.modifier.PlaceSelfCenter
import koala.modifier.SmallIconHeight
import koala.modifier.TextShadow
import koala.modifier.TextSmall
import koala.modifier.WhiteSpaceNoWrap
import koala.modifier.modify
import koala.modifier.setStyle
import kotlinx.html.DIV
import kotlinx.html.img

fun DIV.configureThumbMarker(marker: ThumbMarker) {
    with(marker) {
        markerBodyRow {
            markerThumb()
            markerLabelColumn {
                markerLabel()
                markerSublabel()
            }
        }
    }
}

fun DIV.configureIconMarker(marker: IconMarker) {
    with(marker) {
        markerBodyRow {
            markerIcon()
            markerLabel()
        }
    }
}

/** Draws [marker] as its icon. */
fun DIV.configureTravelMarker(marker: TravelMarker) {
    icon(marker.icon)
}

context(marker: StaticMarker)
fun DIV.markerBodyRow(content: DIV.() -> Unit) {
    row(modify(MarkerStyle.Body, Gap2Px, AlignItemsCenter, TextShadow, WhiteSpaceNoWrap)) {
        marker.themeColor?.let {
            setStyle(Css.ColorScheme.of(it))
        }
        content()
    }
}

context(marker: ThumbMarker)
fun DIV.markerThumb() {
    box(MarkerStyle.Thumb) {
        img {
            src = marker.thumbUrl.value
        }
    }
}

context(marker: StaticMarker)
fun DIV.markerLabel() {
    marker.label?.let {
        textBlock(it, MarkerStyle.LabelMod)
    }
}

context(marker: IconMarker)
fun DIV.markerIcon() {
    box(modify(MarkerStyle.Icon)) {
        icon(marker.svg, modify(SmallIconHeight, PlaceSelfCenter, ColorSchemeFg))
    }
}

fun DIV.markerLabelColumn(content: DIV.() -> Unit) {
    column(modify(Gap0, LineHeight115, WhiteSpaceNoWrap)) {
        content()
    }
}

context(marker: ThumbMarker)
fun DIV.markerSublabel() {
    if (marker.sublabel != null || marker.typeLabel != null) {
        textBlock(mod = TextSmall) {
            marker.typeLabel?.let {
                span(it, modify(ColorSchemeFg, Bold))
            }
            marker.sublabel?.let {
                if (marker.typeLabel != null) span(" • ", OpacityHalf)
                span(it, OpacityHigh)
            }
        }
    }
}