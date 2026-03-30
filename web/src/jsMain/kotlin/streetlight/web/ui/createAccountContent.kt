package streetlight.web.ui

import koala.LottieFile
import koala.css.AlignItemsStretch
import koala.css.PlaceItemsCenter
import koala.css.FlexItems1
import koala.css.MaxWidth50P
import koala.css.QueryRowReverse
import koala.css.modify
import koala.dom.*
import streetlight.web.model.Streetlight
import streetlight.web.model.UserCreator

fun RenderContext.createAccountContent(app: Streetlight) {
    val gate = app.gate
    val creator = UserCreator(renderScope, gate, app.client.api)

    column(modify(QueryRowReverse, FlexItems1, AlignItemsStretch)) {
        card {
            textBlock("Not yet on Streetlight? Create a new account.")
            textField(
                label = "username",
                placeholder = "username",
                bindFlow = creator.usernameFlow,
                onChangeValue = creator::setUsername
            )
            textField(
                label = "email",
                placeholder = "email (optional)",
                bindFlow = creator.emailFlow,
                onChangeValue = creator::setEmail
            )
            textBlock("Your email address is optional. It can be used to reset your password. " +
                    "Streetlight will never contact you without your request.")
            textField(
                label = "password",
                placeholder = "password",
                bindFlow = creator.passwordFlow,
                onChangeValue = creator::setPassword
            )
            textField(
                label = "confirm password",
                placeholder = "confirm password",
                bindFlow = creator.confirmPasswordFlow,
                onChangeValue = creator::setConfirmPassword
            )
            textBlock(
                binding = creator.isValidFlow,
                provideValue = { if (it) "✅" else "❌"}
            )
            button("Sign up", onClick = creator::createAccount, bindIsEnabled = creator.isValidFlow)
        }
        box(modify(PlaceItemsCenter)) {
            column(modify(MaxWidth50P)) {
                lottie(LottieFile.Cat)
                textBlock("Streetlight is at an early stage in development. Only the bravest souls should enter.")
            }
        }
    }
}