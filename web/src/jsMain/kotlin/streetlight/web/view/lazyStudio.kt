package streetlight.web.view

import js.lazy.Lazy
import js.lazy.LazyFunction
import koala.dom.RouteScope

/** The portal of the Studio section, loaded from its own chunk by [lazyRoute]. */
@Lazy
val lazyStudio = LazyFunction<RouteScope.() -> Unit> {
    { viewStudioPortal() }
}
