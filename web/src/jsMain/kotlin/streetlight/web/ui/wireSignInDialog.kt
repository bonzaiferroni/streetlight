package streetlight.web.ui

import kampfire.model.reactIn
import kampfire.model.setFalse
import kampfire.model.storeOf
import koala.dom.ViewScope
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.rawDialogContent
import koala.dom.tabs
import koala.html.Id

fun ViewScope.wireSignInDialog() {
    session.starState.reactIn(contentScope) { star ->
        if (star != null) {
            SignIn.isOpen.setFalse()
        }
    }

    dialog(SignIn.isOpen) {
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
}

object SignIn {
    val isOpen = storeOf(false)
}