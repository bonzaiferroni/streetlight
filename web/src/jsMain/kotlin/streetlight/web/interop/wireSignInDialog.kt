package streetlight.web.interop

import kampfire.model.reactIn
import kampfire.model.setFalse
import kampfire.model.setTrue
import kampfire.model.storeOf
import koala.dom.ViewScope
import koala.dom.component.dialog
import koala.dom.component.dialogCard
import koala.dom.layout.lazyTabs
import koala.dom.component.rawDialogContent
import koala.page.Id
import streetlight.model.data.Star
import streetlight.web.view.fullRegistrationForm
import streetlight.web.model.getUserCreator
import streetlight.web.view.guestRegistrationForm
import streetlight.web.model.session
import streetlight.web.view.signInForm

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
            context(model) {
                lazyTabs(Id("sign-in-tabs")) { // mod = Width64
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