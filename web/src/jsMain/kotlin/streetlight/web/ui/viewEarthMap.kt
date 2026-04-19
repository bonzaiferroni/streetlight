package streetlight.web.ui

import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.btn
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
            }
        }
        geoMapMount(app.geoMap, app.appScope)
    }

    renderScope.launch {
        model.listingFlow.collect { listing ->
            app.streetMap.setPosts(listing?.events)
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

