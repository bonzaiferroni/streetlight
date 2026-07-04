package koala.html

import koala.css.*
import kotlinx.html.FlowContent

object RouteMenu {
    val Mod = Class("route-menu")
    val Route = Class("route-menu__route")
    val RouteNow = Class("route-menu__route-now")
}

fun FlowContent.routeMenu(
    routeNow: AppRoute,
    routes: List<AppRoute>,
    mod: ModifierSet? = null,
) {
    row(modify(mod, RouteMenu.Mod, Gap0, TextTransformUppercase, TextSmall, Padding1, Bold)) {
        routes.forEach { route ->
            when (route == routeNow) {
                true -> span(modify(RouteMenu.RouteNow)) {
                    +route.label
                }
                else -> navigation(route, modify(RouteMenu.Route)) {
                    +route.label
                }
            }
        }
    }
}

//language="CSS"
val NavMenuCss get() = with(RouteMenu) { """
$Mod {
    background-color: rgba(var(--black), .6);
    backdrop-filter: blur(3px);
    -webkit-backdrop-filter: blur(3px);
    position: fixed; 
    bottom: 0;
    left: 0;
    right: 0;
    margin-bottom: var(--unit-spacing-2); 
    width: fit-content;
    margin-inline: auto;
    border-radius: 9999px;
    color: rgb(var(--white));
    user-select: none;
    text-shadow: var(--btn-text-shadow);
    
    $Route, $RouteNow {
        padding: var(--unit-spacing) var(--unit-spacing-2);
        border-radius: 9999px;
    }
    
    $RouteNow {
        background-color: rgba(var(--primary), .2);
    }
    
    $Route {
        color: rgba(var(--white), .8);
    }
}
""" }