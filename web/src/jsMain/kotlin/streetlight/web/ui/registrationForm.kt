package streetlight.web.ui

import kampfire.api.Username
import kampfire.model.AccountType
import koala.LottieFile
import koala.css.Accent
import koala.css.AlignItemsStart
import koala.css.*
import koala.dom.*
import koala.html.GridColumns
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.html.span
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import streetlight.web.model.UserCreatorState

fun AppScope.guestRegistrationForm() {
    val model = app.getUserCreator(parentScope)

    gridColumns(queryTemplate = GridTemplateColumns(1.fr), mod = modify(Gap2)) {
        column {
            heading3("Guest accounts", modify(TextAlignCenter))
            textBlock(guestAccountIntro1)
            textBlock {
                span(guestAccountIntro2, modify(Flex1))
                navigation(text = "→ Learn More")
            }
        }
        card(modify(PrimaryCardBg)) {
            filigree {
                textBlock("How it works", modify(OpacityHigh, Italic))
            }
            bulletsOf(
                modify(Gap1),
                { textBlock("Once you sign out or clear your browser's cookies, you won't be able to sign in again with the same identity.") },
                { textBlock("Guest accounts are automatically deleted after 30 days without activity.") },
                {
                    textBlock {
                        span("Please note: ", modify(OpacityHigh, Italic))
                        span("Anyone else with access to this device can also control the account.")
                    }
                }
            )
        }
    }

    column(modify(QueryContainer)) {
        formPart("A username is all you need to get started.", info = {
            row {
                button("Choose for me", modify(Zen), onClick = model::generateUsername)
            }
        }) {
            formTextField("username", model::setUsername, model.usernameFlow, maxLength = Username.MAX_LENGTH)
        }
        formPart("To create a Streetlight account, you must be ${UserCreatorState.MINIMUM_AGE} or older.") {
            row(modify(JustifyContentSpaceBetween, AlignItemsCenter)) {
                checkBox("I am ${UserCreatorState.MINIMUM_AGE} or older.")
                button("Register as guest", modify(Accent), onClick = { model.createAccount(AccountType.Guest) })
            }
        }
    }
}

val guestAccountIntro1 = """
Guest accounts are temporary and make it easy to participate in Streetlight events and submit content. 
"""
val guestAccountIntro2 =
    "If you decide to stick around, you can make the account permanent and more secure by adding a password. "

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