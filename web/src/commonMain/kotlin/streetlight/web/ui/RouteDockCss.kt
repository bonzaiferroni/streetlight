package streetlight.web.ui

import koala.modifier.*

object RouteDockStyle {
    val Container = Class("route-dock")
    val Title = Container.withBemElement("title")
    val Left = Container.withBemElement("left")
    val Main = Container.withBemElement("main")
    val Right = Container.withBemElement("right")
    val Glass = Container.withBemElement("glass")

    val RouteNow = Container.withBemElement("route-now")
}

//language="CSS"
val RouteDockCss get() = with(RouteDockStyle) { """
$Container {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    grid-template-areas: ". title ." "left main right";
    align-items: center;
    column-gap: var(--unit);
}

$Title { grid-area: title; }
$Left  { grid-area: left; }
$Main  { grid-area: main; }
$Right { grid-area: right; }

$Glass    { color: rgba(var(--ink), .8); background-color: rgba(var(--paper), .6); }
$RouteNow { color: rgb(var(--ink)); outline: 2px solid rgb(var(--primary)); }
""" }
