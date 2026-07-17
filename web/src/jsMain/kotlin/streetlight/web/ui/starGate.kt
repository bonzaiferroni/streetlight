package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.DialogElement
import koala.dom.button
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.flowBlock
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.Star

fun ViewScope.starGate(
    openInitially: Boolean = true,
    baseContent: (ViewScope.(DialogElement) -> Unit)? = null,
    content: ViewScope.(Star) -> Unit
) {
    flowBlock(session.starFlow) { user ->
        when (user) {
            null -> {
                val dialog = dialog("Sign In") {
                    val model = app.getUserCreator(parentScope)
                    tabs { // mod = modify(Width64)
                        tab("guest") {
                            dialogCard {
                                guestRegistrationForm(model)
                            }
                        }
                        tab("register") {
                            dialogCard {
                                fullRegistrationForm(model)
                            }
                        }
                        tab("sign in") {
                            dialogCard {
                                signInForm(model)
                            }
                        }
                    }
                }
                if (openInitially) dialog.open()
                when (baseContent) {
                    null -> {
                        button(onClick = dialog::open) {
                            textBlock("Sign in to continue.")
                        }
                    }
                    else -> baseContent(dialog)
                }
            }
            else -> content(user)
        }
    }
}

