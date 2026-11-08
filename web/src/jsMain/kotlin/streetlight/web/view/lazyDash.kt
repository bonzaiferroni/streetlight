package streetlight.web.view

import js.lazy.Lazy
import js.lazy.LazyFunction
import koala.dom.RouteScope

/** The portal of the Dash section, loaded from its own chunk by [lazyRoute]. */
@Lazy
val lazyDash = LazyFunction<RouteScope.() -> Unit> {
    { viewDashPortal() }
}
