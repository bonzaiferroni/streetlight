package streetlight.web.ui

import kampfire.model.medium
import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.featureImage
import koala.html.heading3
import koala.html.logo
import koala.html.spacer
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.data.GalaxyPost
import streetlight.web.EarthRoute
import streetlight.web.model.EarthMap
import streetlight.web.pages.AppBodyKey

fun RenderContext.viewEarthMap(model: EarthMap) {
    div(Earth.Id, modify(Size100P)) {
        geoMapMount(geoMap, appScope, mod = modify(Earth.Map))
        earthHeader(model)
        earthWindow(model)
        earthPanel(model)
    }
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
    val iconMod = modify(Width5, Aspect1, ZIndex2)
    row(modify(Earth.Header, AlignItemsCenter, PaperGradientBg, Padding1, ZIndex2)) {
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
        icon(SvgFile.Settings, iconMod)
    }
}

fun RenderContext.earthWindow(model: EarthMap) {
    column(modify(Earth.Window, ZIndex2, PointerEventsNone)) {
        spacer(modify(Flex1))
        textBlock(model.summaryFlow.map { map -> map?.entries?.joinToString(" • ") { "${it.key.label}: ${it.value.size}" } })
    }
}

fun RenderContext.earthPanel(model: EarthMap) {
    flowBlock(model.postsFlow, modify(Earth.Panel, ZIndex2)) { posts ->
        when (posts.isEmpty()) {
            true -> galaxyListPanel(model)
            else -> postListPanel(model, posts)
        }
    }
}

// val route = galaxy?.let { GalaxyRoute(it.slug) } ?: HomeRoute
//            btn("View Feed", route, modify(ZIndex2))

fun RenderContext.galaxyListPanel(model: EarthMap) {
    flowBlock(model.galaxiesFlow, modify(Height100P)) { galaxies ->
        column(modify(Padding1)) {
            galaxies.forEach { galaxy ->
                if (galaxy.eventCount + galaxy.locationCount == 0) return@forEach
                navigation(EarthRoute(galaxy.slug), modify(Width100P)) {
                    row(modify(Height8, BorderRadius2, OverflowClip, Gap0)) {
                        image(galaxy.images.thumb, modify(Aspect1))
                        column(modify(PaperGradientBg, Padding1, Gap0)) {
                            heading3(galaxy.name, modify(Bold, LineHeight115, SingleLine))
                            textBlock(buildString {
                                if (galaxy.eventCount > 0) append("${galaxy.eventCount} events")
                                if (galaxy.locationCount > 0) {
                                    if (isNotEmpty()) append(" • ")
                                    append("${galaxy.locationCount} locations")
                                }
                            }, modify(SmallText))
                        }
                    }
                }
            }
        }
    }
}

fun RenderContext.postListPanel(model: EarthMap, posts: List<GalaxyPost>) {
    row(modify(Height100P, Gap0, BorderRadiusTop1, OverflowClip)) {
        selectionBlock(model.postsFlow, model::setPost, model.postFlow, modify(Padding1, CardBg)) { post ->
            image(post.images.thumb, modify(BorderRadius1, Height8))
        }
        flowBlock(model.postFlow, modify(Flex1)) { post ->
            when (post) {
                null -> {
                    column(modify(PaddingTop1, CardGradientBg, Width32)) {
                        posts.forEach { post ->
                            column(modify(Gap0, Height8)) {
                                heading3(post.label, modify(Bold, LineHeight115, SingleLine))
                                post.sublabel?.let {
                                    textBlock(it, modify(OpacityMost))
                                }
                            }
                        }
                    }
                }
                else -> postPanel(post)
            }
        }
    }
}

fun RenderContext.postPanel(post: GalaxyPost) {
    column(modify(Height100P, OverflowYAuto, CardBg, BlurBackdrop)) {
        featureImage(post.images.medium, modify(Width100P, Height24))
        column(modify(Padding1)) {
            heading3(post.label)
            post.description?.let {
                markdown(it)
            }
        }
    }
}
