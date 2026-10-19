package streetlight.web.view

import js.lazy.Lazy
import js.lazy.LazyFunction
import koala.dom.RouteScope

@Lazy
val lazyOnboard = LazyFunction<RouteScope.() -> Unit> {
    { viewOnboardRoute() }
}