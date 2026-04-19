package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.Id
import koala.html.btn
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import streetlight.web.EarthRoute
import streetlight.web.HomeRoute
import streetlight.web.model.Streetlight
import streetlight.web.pages.AppBodyKey

fun ViewContext<Streetlight>.viewEarth() {
    val app = model

    box(EarthKey.id, modify(Size100P)) {
        column(modify(Padding1)) {
            row(modify(JustifyContentCenter)) {
                btn("Home", HomeRoute, modify(ZIndex2))
            }
        }
        geoMapMount(app.geoMap, app.appScope)
    }
}

fun ViewContext<Streetlight>.viewEarthRoute() {
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
                is EarthRoute -> {
                    if (!isVisible) {
                        val scope = createScope()
                        wireBlock(element, scope = scope) {
                            viewContextOf(app) {
                                viewEarth()
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

