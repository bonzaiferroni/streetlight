package streetlight.web.ui

import kampfire.model.AccountType
import koala.LottieFile
import koala.css.AlignItemsStretch
import koala.css.PlaceItemsCenter
import koala.css.FlexItems1
import koala.css.MaxWidth50P
import koala.css.QueryContainer
import koala.css.QueryRowReverse
import koala.css.modify
import koala.dom.*
import koala.html.bulletsOf

fun AppScope.guestRegistrationForm() {
    val model = app.getUserCreator(parentScope)

    column(modify(QueryContainer)) {
        textBlock(guestAccountIntro1)
        bulletsOf(
            "Once you sign out or clear your browser's settings, you won't be able to sign in again.",
            "Guest accounts are automatically deleted after 30 days without activity.",
            "Anyone with access to this device can also access the account."
        )

        textBlock("After creating a guest account, you can upgrade to a registered account at any time by choosing a password.")

        formPart(
            "A username is the only thing required to create an account.",
            bullets = listOf(
                guestAccountIntro1,
                guestAccountBullet1,
            )
        ) {
            formTextField("username", model::setUsername, model.usernameFlow)
        }

    }
}

val guestAccountIntro1 = """
Guest accounts are temporary, they are useful for participating in Streetlight events and submitting content.
"""

val guestAccountBullet1 = """
Guest accounts are less secure than registered accounts and may be lost if you clear your browser's storage.
"""

fun AppScope.fullRegistrationForm() {
    val model = app.getUserCreator(parentScope)

    column(modify(QueryRowReverse, FlexItems1, AlignItemsStretch)) {
        textBlock("Not yet on Streetlight? Create a new account.")
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
            textBlock(
                "Your email address is optional. It can be used to reset your password. " +
                        "Streetlight will never contact you without your request."
            )
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
                provideValue = { if (it) "✅" else "❌" }
            )
            val button = button("Sign up", onClick = { model.createAccount(AccountType.Registered) })
            configureEnabledFlow(button, model.isValidFlow)
        }
        box(modify(PlaceItemsCenter)) {
            column(modify(MaxWidth50P)) {
                lottie(LottieFile.Cat)
                textBlock("Streetlight is at an early stage in development, please report the bugs.")
            }
        }
    }
}