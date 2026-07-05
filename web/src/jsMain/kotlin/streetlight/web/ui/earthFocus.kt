package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.model.Url
import kampfire.model.medium
import koala.css.BlurBackdrop
import koala.css.Bold
import koala.css.BorderRadius2
import koala.css.BorderSolid2Px
import koala.css.Flex1
import koala.css.FlexWrap
import koala.css.Gap0
import koala.css.Height100P
import koala.css.Magic
import koala.css.OpacityHigh
import koala.css.OverflowYAuto
import koala.css.Padding0
import koala.css.Padding1
import koala.css.PointerEventsAuto
import koala.css.Property
import koala.css.TextAlignCenter
import koala.css.Zen
import koala.css.modify
import koala.css.setStyle
import koala.dom.AppScope
import koala.dom.card
import koala.dom.column
import koala.dom.div
import koala.dom.flowBlock
import koala.dom.markdown
import koala.dom.navigation
import koala.dom.row
import koala.dom.tab
import koala.dom.tabs
import koala.html.AppRoute
import koala.html.btn
import koala.html.featureImage
import koala.html.filigree
import koala.html.heading3
import koala.html.heading4
import koala.html.navigationIfNotNull
import koala.model.ClusterFocus
import koala.model.MarkerFocus
import koala.model.PointMarker
import kotlinx.html.FlowContent
import kotlinx.html.hr
import streetlight.model.data.ExtraLink
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.cellContentOf
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.locationRoute
import streetlight.web.model.Earth
import streetlight.web.model.EventMarker
import streetlight.web.model.FeatureMarker

fun AppScope.earthFocus(model: Earth) {
    flowBlock(model.focusFlow, modify(EarthStyle.Focus, Magic)) { focus ->
        when (focus) {
            is ClusterFocus -> tabs(
                mod = modify(PointerEventsAuto, Height100P),
                viewportMod = modify(Flex1, OverflowYAuto, BorderRadius2)
            ) {
                focus.members.forEachIndexed { index, marker ->
                    val tabName = (marker as? FeatureMarker)?.markerType?.name ?: marker.label ?: return@forEachIndexed
                    tab("${index + 1}. $tabName") {
                        markerPanel(marker)
                    }
                }
            }

            is MarkerFocus -> div(modify(Height100P, OverflowYAuto, BorderRadius2)) {
                markerPanel(focus.marker)
            }
            null -> return@flowBlock
        }
    }
}

private fun AppScope.markerPanel(marker: PointMarker) {
    when (marker) {
        is EventMarker -> {
            val event = marker.event
            focusPanel(
                label = event.label,
                sublabel = event.sublabel,
                imageUrl = event.images.medium,
                description = event.body,
                route = event.eventRoute,
                subRoute = event.locationRoute,
                colorScheme = ColorScheme.Accent,
                extraLinks = event.links,
                cells = cellContentOf(event)
            )
        }
    }
}

private fun AppScope.focusPanel(
    label: String,
    sublabel: String?,
    imageUrl: Url?,
    description: Markdown?,
    route: AppRoute,
    subRoute: AppRoute?,
    colorScheme: ColorScheme = ColorScheme.Primary,
    extraLinks: List<ExtraLink>? = null,
    cells: (FlowContent.() -> Unit)? = null,
) {
    card(modify(Gap0, Padding0, BlurBackdrop, PointerEventsAuto, BorderSolid2Px, EarthStyle.MoveDimmer)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
        column(modify(Gap0)) {
            featureImage(imageUrl, modify(Flex1))
            cells?.let {
                cellBlock(modify(FlexWrap), cells)
            }
        }
        column(modify(Padding1)) {
            column(modify(Gap0, TextAlignCenter)) {
                navigation(route) {
                    heading3(label, modify(Bold))
                }
                sublabel?.let {
                    navigationIfNotNull(subRoute) {
                        heading4(sublabel, modify(OpacityHigh))
                    }
                }
            }
            if (extraLinks != null) {
                filigree {
                    row {
                        extraLinks.forEach {
                            btn(it.label, it.url, modify(Zen))
                        }
                    }
                }
            } else {
                hr { }
            }
            description?.let {
                markdown(it, modify(Padding1))
            }
        }
    }
}