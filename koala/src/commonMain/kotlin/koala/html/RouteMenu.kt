package koala.html

import koala.Svg
import koala.css.*
import kotlinx.html.FlowContent

fun FlowContent.routeMenu(
    context: String,
    routeNow: AppRoute,
    routes: List<AppRoute>,
    mod: ModifierSet? = null,
    leftIcons: List<RouteMenuIcon>? = null,
    rightIcons: List<RouteMenuIcon>? = null
) {
    column(modify(RouteMenu.Base, TextTransformUppercase, TextSmall, Gap0, AlignItemsCenter)) {
        filigree(modify(AlignSelfStretch)) {
            textBlock(context)
        }
        row(modify(mod, RouteMenu.ContextMenu, Gap0, Padding1, Bold, AlignItemsCenter, BorderSolid2Px)) {
            leftIcons?.let { icons ->
                iconsTray(icons, modify(RouteMenu.LeftTray))
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
            rightIcons?.let { icons ->
                iconsTray(icons, modify(RouteMenu.RightTray))
            }
        }
    }
}

internal fun FlowContent.iconsTray(
    icons: List<RouteMenuIcon>,
    mod: ModifierSet
) {
    row(modify(mod, BorderSolid2Px)) {
        icons.forEach {
            navigation(it.route, modify(Height3)) {
                icon(it.svg, modify(Height100P))
            }
        }
    }
}

object RouteMenu {
    val Base = Class("route-menu")
    val ContextMenu = Class("route-menu__context-menu")
    val Route = Class("route-menu__route")
    val RouteNow = Class("route-menu__route-now")
    val Back = Class("route-menu__back")
    val LeftTray = Class("route-menu__left-tray")
    val RightTray = Class("route-menu__right-tray")
}

data class RouteMenuIcon(
    val svg: Svg,
    val route: AppRoute
)

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
    opacity: 1;
    transition: opacity 400ms var(--magic-easing), transform 400ms var(--magic-easing);
    transform: scale(1);
    
    
    @starting-style {
        opacity: 0;
        transform: scale(.95);
    }
}

#shell-box {
    $Base {
        display: none;
    }
}

$ContextMenu, $LeftTray, $RightTray {
    backdrop-filter: blur(3px);
    -webkit-backdrop-filter: blur(3px);
    border-radius: 9999px;
    color: rgba(var(--ink), .8);
    background-color: rgba(var(--paper), .6);
}
    
$ContextMenu {
    position: relative;
    
    $Route, $RouteNow {
        padding: var(--unit-spacing) var(--unit-spacing-2);
        border-radius: 9999px;
    }
    
    $RouteNow {
        outline: 2px solid rgb(var(--primary));
        color: rgb(var(--ink));
    }
}

$LeftTray {
    position: absolute;
    right: 100%;
    top: 50%;
    translate: 0 -50%;
    margin-right: var(--unit-spacing);
    padding: var(--unit-spacing);
}

$RightTray {
    position: absolute;
    left: 100%;
    top: 50%;
    translate: 0 -50%;
    margin-left: var(--unit-spacing);
    padding: var(--unit-spacing);
}
""" }