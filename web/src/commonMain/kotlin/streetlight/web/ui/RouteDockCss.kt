package streetlight.web.ui

import koala.modifier.*

object RouteDockStyle {
    val Glass = Class("route-dock-glass")
}

//language="CSS"
val RouteDockCss get() = with(RouteDockStyle) { """
$Glass { color: rgba(var(--ink), .8); background-color: rgba(var(--paper), .6); }
""" }
