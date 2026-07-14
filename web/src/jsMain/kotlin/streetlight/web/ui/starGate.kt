package streetlight.web.ui

import koala.css.Width64
import koala.css.modify
import koala.dom.AppScope
import koala.dom.DialogElement
import koala.dom.button
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.flowBlock
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.Star

fun AppScope.starGate(
    openInitially: Boolean = true,
    baseContent: (AppScope.(DialogElement) -> Unit)? = null,
    content: AppScope.(Star) -> Unit
) {
    flowBlock(session.starFlow) { user ->
        when (user) {
            null -> {
                val dialog = dialog("Sign In") {
                    tabs { // mod = modify(Width64)
                        tab("guest") {
                            dialogCard {
                                guestRegistrationForm()
                            }
                        }
                        tab("register") {
                            dialogCard {
                                fullRegistrationForm()
                            }
                        }
                        tab("sign in") {
                            dialogCard {
                                signInForm()
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