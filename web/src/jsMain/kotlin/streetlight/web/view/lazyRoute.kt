package streetlight.web.view

import js.lazy.LazyFunction
import koala.dom.RouteScope
import koala.dom.launchEffect

/** Builds the route's view with the route view [loader] provides, once its chunk has loaded. */
fun RouteScope.lazyRoute(loader: LazyFunction<RouteScope.() -> Unit>) {
    val routeScope = this
    launchEffect(RouteScope::lazyRoute) {
        val view = loader()
        routeScope.rebuildContent {
            RouteScope(this, routeScope.inflator, routeScope.state).view()
        }
    }
}
