package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.button
import koala.dom.flowBlock
import koala.dom.textBlock
import kampfire.model.setTrue
import koala.modifier.ModifierSet
import streetlight.model.data.Star

fun ViewScope.starGate(
    mod: ModifierSet? = null,
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

fun ViewScope.signInToContinue() {
    button(onClick = { SignIn.isOpen.setTrue() }) {
        textBlock("Sign in to continue.")
    }
}