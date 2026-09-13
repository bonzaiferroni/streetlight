package streetlight.web.ui

import kampfire.api.Username
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.flowBlock
import koala.dom.rawDialogContent
import koala.dom.textBlock
import koala.html.Id
import kampfire.model.MutableTap
import kampfire.model.setTrue
import kampfire.model.storeOf
import koala.css.ModifierSet
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