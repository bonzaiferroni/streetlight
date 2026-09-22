package streetlight.web.ui

import koala.modifier.*

object RouteDockStyle {
    val Container = Class("route-dock")
    val Glass = Container.withBemElement("glass")
    val RouteNow = Container.withBemElement("route-now")
}

//language="CSS"
val RouteDockCss get() = with(RouteDockStyle) { """
$Glass    { color: rgba(var(--ink), .8); background-color: rgba(var(--paper), .6); }
$RouteNow { color: rgb(var(--ink)); outline: 2px solid rgb(var(--primary)); }
""" }
