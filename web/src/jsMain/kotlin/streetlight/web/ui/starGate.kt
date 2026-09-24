package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.button
import koala.dom.flowBlock
import koala.dom.textBlock
import kampfire.model.setTrue
import koala.modifier.*
import streetlight.model.data.Star

/** Renders [content] for the signed-in star, or [baseContent] while no star is signed in. */
fun ViewScope.starGate(
    mod: Modifier? = null,
    baseContent: ViewScope.() -> Unit = ViewScope::signInToContinue,
    content: ViewScope.(Star) -> Unit
) {
    flowBlock(session.starState, mod) { star ->
        when (star) {
            null -> baseContent()
            else -> content(star)
        }
    }
}

/** A button that opens the sign-in dialog. */
fun ViewScope.signInToContinue() {
    button(onClick = { SignIn.isOpen.setTrue() }) {
        textBlock("Sign in to continue.")
    }
}