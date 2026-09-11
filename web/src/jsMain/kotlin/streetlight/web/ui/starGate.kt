package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.button
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.flowBlock
import koala.dom.rawDialogContent
import koala.dom.tabs
import koala.dom.textBlock
import koala.html.Id
import kampfire.model.MutableTap
import kampfire.model.setTrue
import kampfire.model.storeOf
import streetlight.model.data.Star

fun ViewScope.starGate(
    baseContent: (ViewScope.() -> Unit)? = null,
    content: ViewScope.(Star) -> Unit
) {
    flowBlock(session.starState) { user ->
        when (user) {
            null -> {
                when (baseContent) {
                    null -> {
                        button(onClick = { SignIn.isOpen.setTrue() }) {
                            textBlock("Sign in to continue.")
                        }
                    }
                    else -> baseContent()
                }
            }
            else -> content(user)
        }
    }
}

