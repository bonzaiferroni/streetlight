package koala.html

import koala.modifier.*

object RouteMenu {
    val Base = Class("route-menu")
    val ContextMenu = Base.withBemElement("context-menu")
    val Route = Base.withBemElement("route")
    val RouteNow = Base.withBemElement("route-now")
    val Back = Base.withBemElement("back")
    val LeftTray = Base.withBemElement("left-tray")
    val RightTray = Base.withBemElement("right-tray")
}

//language="CSS"
val NavMenuCss get() = with(RouteMenu) { """
    
$Base {
    position: fixed; 
    bottom: var(--unit-2);
    left: 0;
    right: 0;
    /* margin-bottom: var(--unit-2); */ 
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

$ContextMenu, $LeftTray, $RightTray {
    backdrop-filter: blur(3px);
    -webkit-backdrop-filter: blur(3px);
    border-radius: 9999px;
    color: rgba(var(--ink), .8);
    background-color: rgba(var(--paper), .6);
}
    
$ContextMenu {
    position: relative;
    
    $Route {
        border-radius: 9999px;
    }
    
    /* main route menu */
    > $Route {
        padding: var(--unit) var(--unit-2);
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
    margin-right: var(--unit);
    padding: var(--unit);
}

$RightTray {
    position: absolute;
    left: 100%;
    top: 50%;
    translate: 0 -50%;
    margin-left: var(--unit);
    padding: var(--unit);
}
""" }
