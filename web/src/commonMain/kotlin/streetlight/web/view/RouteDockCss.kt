package streetlight.web.view

import koala.modifier.*

object RouteDockStyle {
    val Container = Class("route-dock")
    val Glass = Container.withBemElement("glass")
    val RouteNow = Container.withBemElement("route-now")
    val Hidden = Container.withBemModifier("hidden")
    val Focus = Glass.withBemModifier("focus")
}

//language="CSS"
val RouteDockCss get() = with(RouteDockStyle) { """
$Container { transition: var(--transition-opacity), var(--transition-transform); }

$Container$Hidden {
    opacity: 0;
    transform: var(--slide-up-initial);
}

$Container$Hidden $Glass { pointer-events: none; }

$Glass {
    color: rgba(var(--ink), .8);
    background-color: rgba(var(--paper), .6);
    pointer-events: auto;
}

/* a focus dock and its tray on the primary background, at the glass's .6 */
$Glass$Focus { background-color: color-mix(in srgb, var(--primary-fg) 25%, rgba(var(--paper), .6)); }

$RouteNow { color: rgb(var(--ink)); outline: 2px solid rgb(var(--primary)); }
""" }
