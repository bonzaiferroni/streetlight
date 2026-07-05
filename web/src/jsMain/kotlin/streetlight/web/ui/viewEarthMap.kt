package streetlight.web.ui

import kampfire.api.Markdown
import kampfire.model.Url
import kampfire.model.handleOutcome
import kampfire.model.medium
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.AppRoute
import koala.html.RouteMenuIcon
import koala.html.btn
import koala.html.featureImage
import koala.html.filigree
import koala.html.heading3
import koala.html.heading4
import koala.html.logo
import koala.html.navigationIfNotNull
import koala.html.span
import koala.model.ClusterFocus
import koala.model.MarkerFocus
import koala.model.PointMarker
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.html.FlowContent
import kotlinx.html.hr
import streetlight.model.data.ExtraLink
import streetlight.web.GalaxyMapRoute
import streetlight.web.GalaxyRoute
import streetlight.web.HomeRoute
import streetlight.web.layouts.ColorScheme
import streetlight.web.layouts.cellBlock
import streetlight.web.layouts.cellContentOf
import streetlight.web.layouts.eventRoute
import streetlight.web.layouts.locationRoute
import streetlight.web.model.EarthMap
import streetlight.web.model.EventMarker
import streetlight.web.model.FeatureMarker
import streetlight.web.model.MarkerType
import streetlight.web.pages.AppBodyKey

fun AppScope.viewEarthMap(model: EarthMap) {
    box(EarthStyle.Id, modify(Size100P)) {
        val cameraController = geoMapMount(mod = modify(EarthStyle.Map))
        column(modify(Gap0, PointerEventsNone)) {
            // earthHeader(model)
            div(modify(EarthStyle.Grid, Padding1, Flex1, MinHeight0)) {
                earthUnboundedOverlay(model, cameraController)
                earthChrome(model)
                // earthList(model)
                earthFocus(model)
            }.flowModifier(model.isFocusedFlow, EarthStyle.IsFocused, parentScope)
        }
    }.flowModifier(model.isMovingFlow, EarthStyle.IsMoving, parentScope)
}

fun AppScope.viewEarthMapRoute() {
    var isVisible = false
    val element = document.getElementById(AppBodyKey.FullScreenId)

    launchEffect {
        portal.routeFlow.collect { route ->
            when (route) {
                is GalaxyMapRoute -> {
                    if (!isVisible) {
                        val galaxy = route.slug?.let {
                            api.readGalaxy(it).handleOutcome(toaster::toast)
                        }
                        element.replaceRender(app, parentScope) {
                            val model = app.getEarthMap(parentScope, galaxy)
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

fun AppScope.earthHeader(model: EarthMap) {
    val iconMod = modify(Width5, Aspect1)
    row(modify(EarthStyle.Header, AlignItemsCenter, PaperGradientBg, Padding1, PointerEventsAuto, BlurBackdrop)) {
        flowBlock(model.galaxyFlow, modify(Flex1)) { galaxy ->
            when (galaxy) {
                null -> row {
                    icon(SvgFile.Helm, iconMod)
                    logo()
                }
                else -> row(modify(AlignItemsCenter)) {
                    icon(SvgFile.ArrowLeft, iconMod).onClick {
                        portal.go(GalaxyMapRoute(null))
                    }
                    heading3(galaxy.name, modify(LineHeight115, SingleLine, Bold))
                }
            }
        }
        icon(SvgFile.GearLarge, iconMod).onClick { portal.go(HomeRoute) }
    }
}

fun AppScope.earthChrome(model: EarthMap) {
    column(modify(EarthStyle.Window, EarthStyle.MoveDimmer, JustifyContentSpaceBetween)) {
        row(modify(JustifyContentEnd, AlignItemsStart)) {
            flowBlock(model.summaryFlow) { summary ->
                if (summary.isNullOrEmpty()) return@flowBlock
                column(modify(WidthFitContent, Gap0)) {
                    filigree {
                        textBlock("In View", modify(OpacityHigh))
                    }
                    row(modify(Gap2)) {
                        summary.forEach { (markerType, count) ->
                            textBlock {
                                when (markerType) {
                                    MarkerType.Event -> span("Events", modify(AccentFg))
                                    MarkerType.Location -> span("Locations", modify(PrimaryFg))
                                    MarkerType.Galaxy -> span("Galaxies", modify())
                                }
                                span(" | ", modify(OpacityLow))
                                span(count.toString())
                            }
                        }
                    }
                }
            }
            button("Show All", modify(Zen, PointerEventsAuto, BlurBackdrop)).onClick(model::showAll)
        }
        flowBlock(model.galaxyFlow, modify(Magic)) { galaxy ->
            val feedRoute = when (galaxy) {
                null -> null
                else -> MenuRoute(GalaxyRoute(galaxy.slug), "Feed")
            }
            val routeNow = when (galaxy) {
                null -> MenuLabel("Galaxies")
                else -> MenuLabel("Map")
            }
            val leftIcons = when (galaxy) {
                null -> listOf(RouteMenuIcon(SvgFile.Home, HomeRoute))
                else -> listOf(RouteMenuIcon(SvgFile.CaretLeft, GalaxyMapRoute(null)))
            }
            routeMenu(
                context = galaxy?.name ?: "Streetlight",
                routeNow = routeNow,
                routes = listOf(feedRoute, routeNow),
                mod = modify(PointerEventsAuto),
                leftIcons = leftIcons
            )
        }
    }
}

fun AppScope.earthFocus(model: EarthMap) {
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

fun AppScope.markerPanel(marker: PointMarker) {
    when (marker) {
        is EventMarker -> {
            val post = marker.post
            focusPanel(
                label = post.label,
                sublabel = post.sublabel,
                imageUrl = post.images.medium,
                description = post.body,
                route = post.event.eventRoute,
                subRoute = post.event.locationRoute,
                colorScheme = ColorScheme.Accent,
                extraLinks = post.links,
                cells = cellContentOf(post.event, post)
            )
        }
    }
}

fun AppScope.focusPanel(
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

// fun TagScope.markerItem(
//    thumb: Url?,
//    label: String,
//    sublabel: String?,
//    onClick: () -> Unit
//) {
//    row(modify(Height8, BorderRadius2, OverflowClip, Gap0, WidthFitContent, PointerEventsAuto)) {
//        image(thumb, modify(Aspect1))
//        column(modify(EarthStyle.ListDetail, PaperGradientBg, Padding1, Gap0)) {
//            heading3(label, modify(Bold, LineHeight115, SingleLine))
//            sublabel?.let {
//                textBlock(sublabel, modify(SmallText))
//            }
//        }
//    }.onClick(onClick)
//}

//fun AppScope.earthList(model: EarthMap) {
//    val reversedItems = model.boundedMarkersFlow.map { it.reversed() } // reverse shows new items on top
//    box(modify(Earth.List, Earth.MoveDimmer)) {
//        itemsBlock(
//            flow = reversedItems,
//            mod = modify(Magic, SlideLeft),
//        ) { marker ->
//            when (marker) {
//                is GalaxyMarker -> {
//                    val galaxy = marker.galaxy
//                    // if (galaxy.eventCount + galaxy.locationCount == 0) return@itemsBlock
//                    markerItem(galaxy.images.thumb, galaxy.name, buildString {
//                        if (galaxy.eventCount > 0) append("${galaxy.eventCount} events")
//                        if (galaxy.locationCount > 0) {
//                            if (isNotEmpty()) append(" • ")
//                            append("${galaxy.locationCount} locations")
//                        }
//                    }) {
//                        portal.go(EarthRoute(marker.galaxy.slug))
//                    }
//                }
//                is EventMarker -> {
//                    markerItem(marker.post.images.thumb, marker.post.label, marker.post.sublabel) {
//                        model.setFocus(marker)
//                    }
//                }
//                is LocationMarker -> {
//                    markerItem(marker.location.images.thumb, marker.location.label, marker.location.sublabel) { }
//                }
//            }
//        }
//    }
//}