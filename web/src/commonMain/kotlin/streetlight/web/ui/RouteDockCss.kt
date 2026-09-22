package streetlight.web.ui

import koala.modifier.*

object RouteDockStyle {
    val Container = Class("route-dock")
    val Glass = Container.withBemElement("glass")
    val RouteNow = Container.withBemElement("route-now")
    val Hidden = Container.withBemModifier("hidden")
}

//language="CSS"
val RouteDockCss get() = with(RouteDockStyle) { """
$Container { transition: var(--transition-opacity), var(--transition-transform); }

$Container$Hidden {
    opacity: 0;
    transform: var(--slide-up-initial);
    pointer-events: none;
}

$Glass    { color: rgba(var(--ink), .8); background-color: rgba(var(--paper), .6); }
$RouteNow { color: rgb(var(--ink)); outline: 2px solid rgb(var(--primary)); }
""" }
