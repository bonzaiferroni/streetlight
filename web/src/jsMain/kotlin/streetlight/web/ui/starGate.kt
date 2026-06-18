package streetlight.web.ui

import kampfire.model.AccountType
import koala.dom.AppScope
import koala.dom.button
import koala.dom.column
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.flowBlock
import koala.dom.row
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.Star

fun AppScope.starGate(block: AppScope.(Star) -> Unit) {
    flowBlock(session.starFlow) { user ->
        when (user) {
            null -> {
                val dialog = dialog("Sign In") {
                    tabs {
                        tab("guest") {
                            console.log(">> rendering guest")
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
                                signInContent()
                            }
                        }
                    }
                }.open()
                button(onClick = dialog::open) {
                    textBlock("Sign in to continue.")
                }
            }
            else -> block(user)
        }
    }
}