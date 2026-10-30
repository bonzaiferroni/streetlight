package streetlight.web.page

import koala.component.box
import koala.component.filigree
import koala.component.heading2
import koala.component.heading3
import koala.component.navigation
import koala.model.WebRoute
import streetlight.model.ui.HomeRoute
import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.span
import streetlight.model.ui.TopBar

/**
 * The top bar of a page showing [bar] on a background reaching to the viewport's edges, its rays reaching to a unit
 * from the helm bar's icons, linking to [route]. It scrolls away with the page, or stays at the top of the viewport
 * over the content while [isSticky].
 */
fun FlowContent.topBar(bar: TopBar, isSticky: Boolean = false, route: WebRoute = HomeRoute()) {
    box(modify(TopBarStyle.Container, TopBarStyle.Sticky.takeIf { isSticky })) {
        val reachMod = modify(Height100Pct, TopBarStyle.Reach)
        when (bar) {
            TopBar.Logo -> appHeader(reachMod, rayMaxWidth = null, route = route)
            is TopBar.Title -> filigree(modify(reachMod, PaddingX2), ruleMod = null) {
                navigation(route) {
                    heading2(bar.text, modify(LineHeight1, MoonShadowText, FocusTarget))
                }
            }
            is TopBar.TwoTone -> rayHeader(reachMod, rayMaxWidth = null) {
                navigation(route) {
                    heading2(mod = modify(LineHeight1, MoonShadowText, FocusTarget)) {
                        span {
                            addModifiers(AccentFg)
                            +bar.accent
                        }
                        span {
                            addModifiers(PrimaryFg)
                            +bar.primary
                        }
                    }
                }
            }
        }
    }
}

object TopBarStyle {
    val Container = Class("top-bar")
    val Sticky = Container.withBemModifier("sticky")
    val Reach = Container.withBemElement("reach")
}

// language="CSS"
val TopBarCss get() = with(TopBarStyle) { """
$Container {
    position: relative;
    /* over the page's content, a local order within the body's layer */
    z-index: 1;
    height: calc(var(--unit) * 8);
    margin-bottom: var(--unit);
}

/* the content spans the viewport, held 6 units in, so with its own padding and the rays' margin they end a unit
   from the helm bar's icons */
$Reach {
    margin-inline: calc(50% - 50vw + var(--unit) * 6);
}

$Container$Sticky {
    position: sticky;
    top: 0;
}

/* the background reaches past the content column to the viewport's edges, where the app viewport clips it */
$Container::before {
    content: "";
    position: absolute;
    inset-block: 0;
    inset-inline: -100vw;
    z-index: -1;
    background-color: rgba(var(--paper), .6);
    backdrop-filter: var(--strong-blur);
}
""" }
