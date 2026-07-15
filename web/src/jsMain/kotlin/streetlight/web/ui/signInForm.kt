package streetlight.web.ui

import koala.LottieFile
import koala.css.AlignItemsCenter
import koala.css.MaxWidth50P
import koala.css.modify
import koala.dom.AppScope
import koala.dom.MessageStore
import koala.dom.checkBox
import koala.dom.column
import koala.dom.lottie
import koala.dom.textField
import kotlinx.html.InputType
import streetlight.web.model.CredentialStore
import streetlight.web.model.StarSession

fun AppScope.signInForm() {
    val gate = app.get<StarSession>()
    val cred = app.get<CredentialStore>()
    val messages = MessageStore()

    form {
        formSection("sign in") {
            column {
                textField(
                    label = "username/email",
                    onValue = cred::setUsername,
                    placeholder = "username/email",
                    flow = cred.usernameFlow
                )
                textField(
                    label = "password",
                    onValue = cred::setPassword,
                    placeholder = "password",
                    flow = cred.passwordFlow
                ) {
                    type = InputType.password
                }
                checkBox("Stay signed in", cred::setStayLoggedIn, cred.stayLoggedInFlow)
            }
        }
        formSubmit("Sign in", { gate.signIn(messages) }, messages)

        column(modify(AlignItemsCenter)) {
            lottie(LottieFile.StrollingMan, modify(MaxWidth50P))
        }
    }
}