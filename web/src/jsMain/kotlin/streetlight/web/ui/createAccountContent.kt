package streetlight.web.ui

import kampfire.model.AccountType
import koala.LottieFile
import koala.css.AlignItemsStretch
import koala.css.PlaceItemsCenter
import koala.css.FlexItems1
import koala.css.MaxWidth50P
import koala.css.QueryRowReverse
import koala.css.modify
import koala.dom.*

fun AppScope.createAccountContent() {
    val model = app.getUserCreator(parentScope)

    column(modify(QueryRowReverse, FlexItems1, AlignItemsStretch)) {
        textBlock("Not yet on Streetlight? Create a new account.")
        tabs {
            tab("Register") {
                card {
                    textField(
                        label = "username",
                        flow = model.usernameFlow,
                        onValue = model::setUsername
                    )
                    textField(
                        label = "email (optional)",
                        placeholder = "email (optional)",
                        flow = model.emailFlow,
                        onValue = model::setEmail
                    )
                    textBlock("Your email address is optional. It can be used to reset your password. " +
                            "Streetlight will never contact you without your request.")
                    textField(
                        label = "password",
                        flow = model.passwordFlow,
                        onValue = model::setPassword
                    )
                    textField(
                        label = "confirm password",
                        flow = model.confirmPasswordFlow,
                        onValue = model::setConfirmPassword
                    )
                    textBlock(
                        binding = model.isValidFlow,
                        provideValue = { if (it) "✅" else "❌"}
                    )
                    val button = button("Sign up", onClick = { model.createAccount(AccountType.Registered) })
                    configureEnabledFlow(button, model.isValidFlow)
                }
            }
            tab("Guest") {
                card {
                    textField(
                        label = "username",
                        flow = model.usernameFlow,
                        onValue = model::setUsername
                    )
                    button("register as guest", onClick = { model.createAccount(AccountType.Guest) })
                }
            }
        }
        box(modify(PlaceItemsCenter)) {
            column(modify(MaxWidth50P)) {
                lottie(LottieFile.Cat)
                textBlock("Streetlight is at an early stage in development, please report the bugs.")
            }
        }
    }
}