package streetlight.web.core

import io.kvision.core.Container

data class PageConfig(
    val name: String,
    val route: String,
    val navLink: Boolean = false,
    val icon: String = "",
    val builder: PageBuilder
) {
    val linkRoute: String get() = "#$routeBeforeId"
    val routeBeforeId: String get() = route.substringBefore("/:")
    fun getIdRoute(id: Int) = route.replace(":id", id.toString())
}

abstract class PageBuilder {
}

data class CachedPageBuilder(
    val content: Container.(AppContext) -> PortalEvents?
) : PageBuilder()

data class IdPageBuilder(
    val content: Container.(AppContext, Int) -> PortalEvents?
) : PageBuilder() {
}

data class TransientPageBuilder(
    val content: Container.(AppContext) -> PortalEvents?
) : PageBuilder()

data class PortalEvents(
    val onLoad: (() -> Unit)? = null,
    val onUnload: (() -> Unit)? = null
)

fun PageConfig.navigate(context: AppContext) = context.routing.navigate(route)
fun AppContext.navigate(config: PageConfig) = routing.navigate(config.route)
fun AppContext.navigate(config: PageConfig, id: Int) = routing.navigate(config.getIdRoute(id))