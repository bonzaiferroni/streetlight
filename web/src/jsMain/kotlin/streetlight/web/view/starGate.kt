package streetlight.web.view

import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.component.filigree
import koala.component.heading4
import koala.dom.component.column
import koala.dom.layout.flowBlock
import koala.modifier.*
import streetlight.model.data.Star
import streetlight.model.ui.TopBar
import streetlight.web.model.getUserCreator
import streetlight.web.model.session
import streetlight.web.page.topBar

/** Renders [content] for the signed-in star, or [baseContent] while no star is signed in. */
fun ViewScope.starGate(
    mod: Modifier? = null,
    baseContent: ViewScope.() -> Unit = ViewScope::signInToContinue,
    content: context(Star) ViewScope.() -> Unit
) {
    flowBlock(session.starState, mod) { star ->
        when (star) {
            null -> baseContent()
            else -> context(star) { content() }
        }
    }
}

/** A [starGate] over a route's whole view, asking the star to sign in on a page with its top bar. */
fun RouteScope.starGate(mod: Modifier? = null, content: context(Star) ViewScope.() -> Unit) =
    starGate(mod, {
        column {
            topBar(TopBar.Logo)
            signInToContinue()
        }
    }, content)

/** The [signInForm] on a card, asking the star to sign in to see the page. */
fun ViewScope.signInToContinue() {
    val creator = app.getUserCreator(contentScope)
    column(modify(AlignSelfCenter, MinWidth(32), BorderRadius3, MoonShadow, Outline, CardBg, Padding(2))) {
        filigree {
            heading4("Sign in to continue")
        }
        context(creator) {
            signInForm()
        }
    }
}