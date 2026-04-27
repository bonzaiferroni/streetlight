package streetlight.web.ui

import kampfire.model.thumb
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.btn
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import streetlight.model.data.EventPost
import streetlight.model.data.LocationPost
import streetlight.web.EarthMapRoute
import streetlight.web.GalaxySlugRoute
import streetlight.web.HomeRoute
import streetlight.web.model.EarthMap
import streetlight.web.model.Streetlight
import streetlight.web.pages.AppBodyKey

fun ViewContext<EarthMap>.viewEarthMap() {
    val app = model.app

    box(EarthKey.Id, modify(Size100P)) {
        flowBlock(model.galaxyFlow) { galaxy ->
            column(modify(Padding1)) {
                row(modify(JustifyContentSpaceBetween)) {
                    galaxyEarthMenu(galaxy, app, modify(ZIndex2))
                    row {
                        icon(SvgFile.Settings, modify(Width5, Aspect1, ZIndex2))
                        val route = galaxy?.let { GalaxySlugRoute(it.slug) } ?: HomeRoute
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
                                            post.event?.locationName?.let {
                                                textBlock(it, modify(OpacityMost, SingleLine))
                                            }
                                        }
                                    }
                                }
                                is LocationPost -> return@forEach
                            }
                        }
                    }
                }
            }
        }
        geoMapMount(app.geoMap, app.appScope)
    }

    renderScope.launch {
        model.postsFlow.collect { posts ->
            app.streetMap.setPosts(posts)
        }
    }
}

fun ViewContext<Streetlight>.viewEarthMapRoute() {
    val app = model
    var isVisible = false
    val element = document.getElementById(AppBodyKey.FullScreenId)
    var job: Job? = null

    fun createScope(): CoroutineScope {
        job = SupervisorJob()
        return CoroutineScope(renderScope.coroutineContext + job)
    }

    renderScope.launch {
        app.portal.routeFlow.collect { route ->
            when (route) {
                is EarthMapRoute -> {
                    if (!isVisible) {
                        val scope = createScope()
                        val model = EarthMap(app, scope)
                        wireBlock(element, scope = scope) {
                            viewContextOf(model) {
                                viewEarthMap()
                            }
                        }
                        element.modify(Reveal)
                        isVisible = true
                    }
                }
                else -> {
                    if (isVisible) {
                        job?.cancel()
                        element.unmodify(Reveal)
                        isVisible = false
                    }
                }
            }
        }
    }
}

