package streetlight.web.view

import js.lazy.Lazy
import js.lazy.LazyFunction
import koala.dom.RouteScope

/** The route view of the sandbox, loaded from its own chunk by [lazyRoute]. */
@Lazy
val lazySandbox = LazyFunction<RouteScope.() -> Unit> {
    { viewSandbox() }
}


