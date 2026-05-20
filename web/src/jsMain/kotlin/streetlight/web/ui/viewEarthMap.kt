package streetlight.web.ui

import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.btn
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import streetlight.model.data.StarPost
import streetlight.model.data.EventPost
import streetlight.model.data.LocationPost
import streetlight.web.EarthMapRoute
import streetlight.web.GalaxyRoute
import streetlight.web.HomeRoute
import streetlight.web.model.EarthMap
import streetlight.web.pages.AppBodyKey

fun RenderContext.viewEarthMap(model: EarthMap) {

    box(EarthKey.Id, modify(Size100P)) {
        flowBlock(model.galaxyFlow) { galaxy ->
            column(modify(Padding1)) {
                row(modify(JustifyContentSpaceBetween)) {
                    galaxyEarthMenu(galaxy, modify(ZIndex2))
                    row {
                        icon(SvgFile.Settings, modify(Width5, Aspect1, ZIndex2))
                        val route = galaxy?.let { GalaxyRoute(it.slug) } ?: HomeRoute
                        btn("View Feed", route, modify(ZIndex2))
                    }
                }
                val posts = model.stateNow.posts?.takeIf { it.isNotEmpty() }
                posts?.let { posts ->
                    card(modify(MaxWidth32, ZIndex2)) {
                        posts.forEach { post ->
                            when (post) {
                                is EventPost -> {
                                    row(modify(Height8, AlignItemsCenter)) {
                                        image(post.images.thumb, modify(Aspect1, Width8, BorderRadius50P))
                                        column(modify(Flex1, Gap0)) {
                                            textBlock(post.title, modify(SingleLine))
                                            textBlock(post.event.locationDisplayTitle, modify(OpacityMost, SingleLine))
                                        }
                                    }
                                }
                                is LocationPost -> return@forEach
                                is StarPost -> return@forEach
                            }
                        }
                    }
                }
            }
        }
        geoMapMount(geoMap, appScope)
    }

    renderScope.launch {
        model.postsFlow.collect { posts ->
            streetMap.setPosts(posts)
        }
    }
}

fun RenderContext.viewEarthMapRoute() {
    var isVisible = false
    val element = document.getElementById(AppBodyKey.FullScreenId)

    renderScope.launch {
        portal.routeFlow.collect { route ->
            when (route) {
                is EarthMapRoute -> {
                    if (!isVisible) {
                        replaceRender(element) {
                            val model = app.getCoroutineScoped<EarthMap>(renderScope)
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

