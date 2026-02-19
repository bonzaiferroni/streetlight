package streetlight.web

import koala.dom.*

fun RenderContext.createAccountCard(app: AppContext) {
    val gate = app.gate
    val creator = UserCreator(renderScope, gate, app.client.api)

    card {
        textBlock("Not yet on Streetlight? Create a new account.")
        textField(
            label = "username",
            placeholder = "username",
            binding = creator.usernameFlow,
            onChangeValue = creator::setUsername
        )
        textField(
            label = "email",
            placeholder = "email",
            binding = creator.emailFlow,
            onChangeValue = creator::setEmail
        )
        textBlock("Your email address is optional. It can be used to reset your password. " +
                "We will never contact you without your request.")
        textField(
            label = "password",
            placeholder = "password",
            binding = creator.passwordFlow,
            onChangeValue = creator::setPassword
        )
        textField(
            label = "confirm password",
            placeholder = "confirm password",
            binding = creator.confirmPasswordFlow,
            onChangeValue = creator::setConfirmPassword
        )
        textBlock(
            binding = creator.isValidFlow,
            provideValue = { if (it) "✅" else "❌"}
        )
        button("Sign up", onClick = creator::createAccount, bindIsEnabled = creator.isValidFlow)
    }
}