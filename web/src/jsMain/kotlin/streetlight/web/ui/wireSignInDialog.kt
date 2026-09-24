package streetlight.web.ui

import kampfire.model.reactIn
import kampfire.model.setFalse
import kampfire.model.setTrue
import kampfire.model.storeOf
import koala.dom.ViewScope
import koala.dom.dialog
import koala.dom.dialogCard
import koala.dom.lazyTabs
import koala.dom.rawDialogContent
import koala.html.Id
import streetlight.model.data.Star

/** The sign-in dialog, opened by [SignIn.isOpen] and closed once a star signs in. */
fun ViewScope.wireSignInDialog() {
    session.starState.reactIn(contentScope) { star ->
        if (star != null) {
            SignIn.isOpen.setFalse()
        }
    }

    dialog(SignIn.isOpen) {
        val model = app.getUserCreator(contentScope)
        rawDialogContent("Sign In") {
            lazyTabs(Id("sign-in-tabs")) { // mod = Width64
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

/** The signed-in star; otherwise opens the sign-in dialog and runs [block], which must leave. */
inline fun ViewScope.starCheck(block: () -> Nothing): Star {
    return when (val star = session.starState.now) {
        null -> {
            SignIn.isOpen.setTrue()
            block()
        }
        else -> star
    }

}

object SignIn {
    val isOpen = storeOf(false)
}