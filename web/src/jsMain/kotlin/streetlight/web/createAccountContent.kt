package streetlight.web

import koala.css.AlignItemsStretch
import koala.css.CenterItems
import koala.css.FlexItems1
import koala.css.MaxWidth50
import koala.css.QueryRowReverse
import koala.css.modify
import koala.dom.*

fun RenderContext.createAccountContent(app: AppContext) {
    val gate = app.gate
    val creator = UserCreator(renderScope, gate, app.client.api)

    column(modify(QueryRowReverse, FlexItems1, AlignItemsStretch)) {
        card {
            textBlock("Not yet on Streetlight? Create a new account.")
            textField(
                label = "username",
                placeholder = "username",
                values = creator.usernameFlow,
                onChangeValue = creator::setUsername
            )
            textField(
                label = "email",
                placeholder = "email (optional)",
                values = creator.emailFlow,
                onChangeValue = creator::setEmail
            )
            textBlock("Your email address is optional. It can be used to reset your password. " +
                    "Streetlight will never contact you without your request.")
            textField(
                label = "password",
                placeholder = "password",
                values = creator.passwordFlow,
                onChangeValue = creator::setPassword
            )
            textField(
                label = "confirm password",
                placeholder = "confirm password",
                values = creator.confirmPasswordFlow,
                onChangeValue = creator::setConfirmPassword
            )
            textBlock(
                binding = creator.isValidFlow,
                provideValue = { if (it) "✅" else "❌"}
            )
            button("Sign up", onClick = creator::createAccount, bindIsEnabled = creator.isValidFlow)
        }
        box(modify(CenterItems)) {
            column(modify(MaxWidth50)) {
                lottie("playful_cat")
                textBlock("Streetlight is at an early stage in development. Only the bravest souls should enter.")
            }
        }
    }
}