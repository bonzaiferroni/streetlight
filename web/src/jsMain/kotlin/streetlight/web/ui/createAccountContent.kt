package streetlight.web.ui

import koala.LottieFile
import koala.css.AlignItemsStretch
import koala.css.PlaceItemsCenter
import koala.css.FlexItems1
import koala.css.MaxWidth50P
import koala.css.QueryRowReverse
import koala.css.modify
import koala.dom.*

fun DOMRender.createAccountContent() {
    val creator = app.getUserCreator(renderScope)

    column(modify(QueryRowReverse, FlexItems1, AlignItemsStretch)) {
        card {
            textBlock("Not yet on Streetlight? Create a new account.")
            textField(
                label = "username",
                placeholder = "username",
                flow = creator.usernameFlow,
                onValue = creator::setUsername
            )
            textField(
                label = "email",
                placeholder = "email (optional)",
                flow = creator.emailFlow,
                onValue = creator::setEmail
            )
            textBlock("Your email address is optional. It can be used to reset your password. " +
                    "Streetlight will never contact you without your request.")
            textField(
                label = "password",
                placeholder = "password",
                flow = creator.passwordFlow,
                onValue = creator::setPassword
            )
            textField(
                label = "confirm password",
                placeholder = "confirm password",
                flow = creator.confirmPasswordFlow,
                onValue = creator::setConfirmPassword
            )
            textBlock(
                binding = creator.isValidFlow,
                provideValue = { if (it) "✅" else "❌"}
            )
            val button = button("Sign up", onClick = creator::createAccount)
            configureEnabledFlow(button, creator.isValidFlow)
        }
        box(modify(PlaceItemsCenter)) {
            column(modify(MaxWidth50P)) {
                lottie(LottieFile.Cat)
                textBlock("Streetlight is at an early stage in development. Only the bravest souls should enter.")
            }
        }
    }
}