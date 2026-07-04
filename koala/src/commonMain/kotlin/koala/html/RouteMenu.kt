package koala.html

import koala.SvgFile
import koala.css.*
import kotlinx.html.FlowContent

fun FlowContent.routeMenu(
    context: String,
    routeNow: AppRoute,
    routes: List<AppRoute>,
    mod: ModifierSet? = null,
    backRoute: AppRoute? = null,
) {
    column(modify(RouteMenu.Base, TextTransformUppercase, TextSmall, Gap0)) {
        filigree {
            textBlock(context)
        }
        row(modify(mod, RouteMenu.Menu, Gap0, Padding1, Bold, AlignItemsCenter, BorderSolid2Px)) {
            backRoute?.let {
                navigation(it, modify(RouteMenu.Back, Height3, OpacityHigh)) {
                    icon(SvgFile.Home, modify(Height100P))
                }
            }
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
}

object RouteMenu {
    val Base = Class("route-context")
    val Menu = Class("route-context__menu")
    val Route = Class("route-context__route")
    val RouteNow = Class("route-context__route-now")
    val Back = Class("route-context__back")
}

//language="CSS"
val NavMenuCss get() = with(RouteMenu) { """
    
$Base {
    position: fixed; 
    bottom: 0;
    left: 0;
    right: 0;
    margin-bottom: var(--unit-spacing-2); 
    width: fit-content;
    margin-inline: auto;
    user-select: none;
}
    
$Menu {
    position: relative;
    background-color: rgba(var(--black), .6);
    backdrop-filter: blur(3px);
    -webkit-backdrop-filter: blur(3px);
    border-radius: 9999px;
    color: rgba(var(--white), .8);
    text-shadow: var(--btn-text-shadow);
    
    $Route, $RouteNow {
        padding: var(--unit-spacing) var(--unit-spacing-2);
        border-radius: 9999px;
    }
    
    $RouteNow {
        outline: 2px solid rgb(var(--primary));
        color: rgb(var(--white));
    }
}

$Back {
    position: absolute;
    right: 100%;
    top: 50%;
    translate: 0 -50%;
    padding-right: var(--unit-spacing)
}
""" }