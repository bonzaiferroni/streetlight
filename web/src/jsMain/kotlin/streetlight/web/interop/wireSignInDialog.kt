package streetlight.web.interop

import kampfire.model.AccountType
import kampfire.model.reactIn
import kampfire.model.setFalse
import kampfire.model.setTrue
import kampfire.model.storeOf
import koala.dom.ViewScope
import koala.dom.component.dialog
import koala.dom.component.dialogCard
import koala.dom.layout.lazyTabs
import koala.dom.component.rawDialogContent
import streetlight.model.data.Star
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formSubmit
import streetlight.web.view.fullRegistrationForm
import streetlight.web.model.SignInPath
import streetlight.web.model.getUserCreator
import streetlight.web.view.guestRegistrationForm
import streetlight.web.model.session

/** The sign-in dialog, opened by [SignIn.isOpen] and closed once a star signs in. */
fun ViewScope.wireSignInDialog() {
    session.starState.reactIn(contentScope) { star ->
        if (star != null) {
            SignIn.isOpen.setFalse()
        }
    }

    dialog(SignIn.isOpen) {
        val model = app.getUserCreator(contentScope)
        rawDialogContent("Create Account") {
            context(model) {
                lazyTabs("sign-in-tabs") { // mod = Width64
                    SignInPath.entries.forEach {
                        tab(it.label, it.svg) {
                            dialogCard {
                                when (it) {
                                    SignInPath.Guest -> guestRegistrationForm()
                                    SignInPath.Register -> formColumn {
                                        fullRegistrationForm()
                                        formSubmit(
                                            label = "Sign up",
                                            onClick = { model.createAccount(AccountType.Registered) },
                                            messenger = model.messages,
                                            enabledTap = model.isValidField
                                        )
                                    }
                                }
                            }
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