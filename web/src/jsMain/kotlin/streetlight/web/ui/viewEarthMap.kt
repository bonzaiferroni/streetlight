package streetlight.web.ui

import kampfire.model.Url
import kampfire.model.medium
import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.featureImage
import koala.html.heading3
import koala.html.heading4
import koala.html.image
import koala.html.logo
import koala.html.spacer
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.html.FlowContent
import kotlinx.html.hr
import streetlight.web.EarthRoute
import streetlight.web.HomeRoute
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.GridArea
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.eventCells
import streetlight.web.model.EarthMap
import streetlight.web.model.EventMarker
import streetlight.web.model.GalaxyMarker
import streetlight.web.model.LocationMarker
import streetlight.web.pages.AppBodyKey

fun RenderContext.viewEarthMap(model: EarthMap) {
    box(Earth.Id, modify(Size100P)) {
        val mount = geoMapMount(geoMap, appScope, mod = modify(Earth.Map))
        column(modify(Gap0, PointerEventsNone)) {
            earthHeader(model)
            div(modify(Earth.Grid, Padding1, Flex1, MinHeight0)) {
                earthUnboundedOverlay(model, mount)
                earthWindow(model)
                earthList(model)
                earthFocus(model)
            }.flowModifier(model.isFocusedFlow, Earth.IsFocused, renderScope)
        }
    }.flowModifier(model.isMovingFlow, Earth.IsMoving, renderScope)
}

fun RenderContext.viewEarthMapRoute() {
    var isVisible = false
    val element = document.getElementById(AppBodyKey.FullScreenId)

    renderScope.launch {
        portal.routeFlow.collect { route ->
            when (route) {
                is EarthRoute -> {
                    if (!isVisible) {
                        replaceRender(element) {
                            val model = app.getEarthMap(renderScope)
                            viewEarthMap(model)
                        }
                        element.modify(Reveal)
                        isVisible = true
                    }
                }
                else -> {
                    if (isVisible) {
                        element.unmodify(Reveal)
                        isVisible = false
                        delay(KoalaTheme.MAGIC_INTERVAL.toLong())
                        clearRender(element)
                    }
                }
            }
        }
    }
}

fun RenderContext.earthHeader(model: EarthMap) {
    val iconMod = modify(Width5, Aspect1)
    row(modify(Earth.Header, AlignItemsCenter, PaperGradientBg, Padding1)) {
        flowBlock(model.galaxyFlow, modify(Flex1)) { galaxy ->
            when (galaxy) {
                null -> row {
                    icon(SvgFile.Helm, iconMod)
                    logo()
                }
                else -> row(modify(AlignItemsCenter)) {
                    icon(SvgFile.ArrowLeft, iconMod).onClick {
                        portal.go(EarthRoute(null))
                    }
                    heading3(galaxy.name, modify(LineHeight115, SingleLine, Bold))
                }
            }
        }
        icon(SvgFile.Settings, iconMod).onClick { portal.go(HomeRoute) }
    }
}

fun RenderContext.earthWindow(model: EarthMap) {
    column(modify(Earth.Window, Earth.MoveDimmer)) {
        spacer(modify(Flex1))
        textBlock(model.summaryFlow.map { map -> map?.entries?.joinToString(" • ") { "${it.key.label}: ${it.value.size}" } })
    }
}

fun RenderContext.earthList(model: EarthMap) {
    val reversedItems = model.boundedMarkersFlow.map { it.reversed() } // reverse shows new items on top
    box(modify(Earth.List, Earth.MoveDimmer)) {
        itemsBlock(
            flow = reversedItems,
            mod = modify(Magic, SlideLeft),
        ) { marker ->
            when (marker) {
                is GalaxyMarker -> {
                    val galaxy = marker.galaxy
                    if (galaxy.eventCount + galaxy.locationCount == 0) return@itemsBlock
                    markerItem(galaxy.images.thumb, galaxy.name, buildString {
                        if (galaxy.eventCount > 0) append("${galaxy.eventCount} events")
                        if (galaxy.locationCount > 0) {
                            if (isNotEmpty()) append(" • ")
                            append("${galaxy.locationCount} locations")
                        }
                    }) {
                        portal.go(EarthRoute(marker.galaxy.slug))
                    }
                }
                is EventMarker -> {
                    markerItem(marker.post.images.thumb, marker.post.label, marker.post.sublabel) {
                        model.setFocus(marker)
                    }
                }
                is LocationMarker -> {
                    markerItem(marker.location.images.thumb, marker.location.label, marker.location.sublabel) { }
                }
            }
        }
    }
}

fun DOMContext.markerItem(
    thumb: Url?,
    label: String,
    sublabel: String?,
    onClick: () -> Unit
) {
    row(modify(Height8, BorderRadius2, OverflowClip, Gap0, WidthFitContent, PointerEventsAuto)) {
        image(thumb, modify(Aspect1))
        column(modify(Earth.ListDetail, PaperGradientBg, Padding1, Gap0)) {
            heading3(label, modify(Bold, LineHeight115, SingleLine))
            sublabel?.let {
                textBlock(sublabel, modify(SmallText))
            }
        }
    }.onClick(onClick)
}

fun RenderContext.earthFocus(model: EarthMap) {
    flowBlock(model.focusFlow, modify(Earth.Focus, Magic, SlideLeft, BorderRadius2, OverflowYAuto)) { marker ->
        when (marker) {
            null -> return@flowBlock
            is EventMarker -> {
                val post = marker.post
                focusPanel(
                    label = post.label,
                    sublabel = post.sublabel,
                    imageUrl = post.images.medium,
                    description = post.description,
                    colorScheme = ColorScheme.Accent,
                    details = eventCells(marker.post.event)
                )
            }
        }
    }
}

fun RenderContext.focusPanel(
    label: String,
    sublabel: String?,
    imageUrl: Url?,
    description: String?,
    colorScheme: ColorScheme = ColorScheme.Primary,
    details: (FlowContent.() -> Unit)? = null,
) {
    card(modify(Gap0, Padding0, BlurBackdrop, PointerEventsAuto, Earth.MoveDimmer)) {
        setStyle(Property.ColorScheme.to(colorScheme.cssValue))
        row(modify(Gap0)) {
            featureImage(imageUrl, modify(Flex1))
            details?.let {
                cellBlock(modify(FlexWrap, Width16), details)
            }
        }
        column(modify(Padding1)) {
            column(modify(Gap0, LineHeight115)) {
                heading3(label, modify(Bold))
                sublabel?.let {
                    heading4(sublabel, modify(OpacityHigh))
                }
            }
            hr { }
            description?.let {
                markdown(it, modify(SmallText))
            }
        }
    }
}