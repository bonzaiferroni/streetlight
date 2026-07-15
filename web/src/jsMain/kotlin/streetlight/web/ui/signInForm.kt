package streetlight.web.ui

import koala.LottieFile
import koala.css.Accent
import koala.css.AlignItemsCenter
import koala.css.FlexItems1
import koala.css.MaxWidth50P
import koala.css.MediaMdRow
import koala.css.modify
import koala.dom.AppScope
import koala.dom.MessageStore
import koala.dom.button
import koala.dom.card
import koala.dom.checkBox
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.lottie
import koala.dom.row
import koala.dom.textField
import kotlinx.html.InputType
import kotlinx.html.js.p
import streetlight.model.ui.HomeRoute
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
        formSubmit("Sign in", { gate.signIn(messages::set) }, messages)

        column(modify(AlignItemsCenter)) {
            lottie(LottieFile.StrollingMan, modify(MaxWidth50P))
        }
    }
}