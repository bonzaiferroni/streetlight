package streetlight.web.model

import koala.dom.ViewScope
import koala.html.AppRoute

data class RouteScope(
    val view: ViewScope,
    val route: AppRoute,
    val inflator: RouteInflator
)