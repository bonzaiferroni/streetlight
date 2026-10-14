package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.layout.flowBlock
import koala.dom.component.textBlock
import kampfire.model.setTrue
import koala.modifier.*
import streetlight.model.data.Star
import streetlight.web.interop.SignIn
import streetlight.web.model.session

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

/** A button that opens the sign-in dialog. */
fun ViewScope.signInToContinue() {
    button(onClick = { SignIn.isOpen.setTrue() }) {
        textBlock("Sign in to continue.")
    }
}