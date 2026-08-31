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
    isOpenState: MutableTap<Boolean> = storeOf(true),
    baseContent: (ViewScope.() -> Unit)? = null,
    content: ViewScope.(Star) -> Unit
) {
    flowBlock(session.starState) { user ->
        when (user) {
            null -> {
                dialog(isOpenState) {
                    val model = app.getUserCreator(contentScope)
                    rawDialogContent("Sign In") {
                        tabs(Id("sign-in-tabs")) { // mod = modify(Width64)
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
                }
                when (baseContent) {
                    null -> {
                        button(onClick = isOpenState::setTrue) {
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

