package streetlight.web.ui

import kampfire.api.Password
import kampfire.api.Username
import kampfire.model.AccountType
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.html.span
import koala.model.MutableField
import kotlinx.html.InputType
import streetlight.web.model.EmailEditor
import streetlight.web.model.PasswordEditor
import streetlight.web.model.UserCreator
import streetlight.web.model.UserCreatorState

fun ViewScope.guestRegistrationForm(model: UserCreator) {
    form {
        formRow {
            column {
                heading3("Guest accounts", modify(TextAlignCenter))
                textBlock(guestAccountIntro1)
                textBlock {
                    span(guestAccountIntro2, modify(Flex1))
                    navigation { +"→ Learn More" }
                }
            }
            card(modify(PrimaryCardBg)) {
                filigree {
                    textBlock("How it works", modify(OpacityHigh, Italic))
                }
                bulletsOf(
                    modify(Gap1),
                    { textBlock("Guest credentials are stored as secure browser cookies, so the account will only be available on this device.") },
                    { textBlock("Clearing your cookies will remove access to the account.") },
                    {
                        textBlock {
                            span("Please note: ", modify(FadeLoop, Italic))
                            span("Anyone else with access to this device can also control the account.")
                        }
                    }
                )
            }
            usernameSection(model)
            requirementsSection(model)
        }
        formSubmit(
            buttonText = "Register as Guest",
            onClick = { model.createAccount(AccountType.Guest) },
            messages = model.messages,
            enabledFlow = model.isValidField.flow
        )
    }
}

val guestAccountIntro1 = """
Guest accounts are temporary and make it easy to participate in Streetlight events and submit content. 
"""
val guestAccountIntro2 = """
Guest accounts and user data are automatically deleted after 30 days without activity. 
If you decide to stick around, you can make the account permanent and more secure by adding a password. 
"""

fun ViewScope.fullRegistrationForm(model: UserCreator) {

    form {
        formRow {
            usernameSection(model)

            emailFormSection(model.emailEditor)
            passwordFormSection(model.passwordEditor)

            requirementsSection(model)
        }
        formSubmit(
            buttonText = "Sign up",
            onClick = { model.createAccount(AccountType.Registered) },
            messages = model.messages,
            enabledFlow = model.isValidField.flow
        )
    }
}

private fun ViewScope.requirementsSection(model: UserCreator) = formSection("Requirements") {
    minAgeToggle(model.minAgeField)
}

private fun ViewScope.usernameSection(model: UserCreator) = formSection("Username") {
    row {
        textField(
            label = null,
            mod = modify(Flex1),
            field = model.usernameField,
            maxLength = Username.MAX_LENGTH,
            placeholder = "Username"
        )
        button("Choose for me", model::generateUsername, modify(Secondary))
    }
    formBullets(null, "Between ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters.")
}

fun ViewScope.minAgeToggle(
    field: MutableField<Boolean>
) = column(modify(AlignItemsCenter)) {
    textBlock("To create an account, you must be 17 or older.", modify(TextAlignCenter))
    checkBox(field, "I am ${UserCreatorState.MINIMUM_AGE} or older.")
}

fun ViewScope.passwordFormSection(model: PasswordEditor) = formSection("Password") {
    textField(model.passwordField, "password") {
        type = InputType.password
    }
    textField(model.confirmationFlow, "confirm password") {
        type = InputType.password
    }
    formBullets(
        null,
        "Must have at least 3: uppercase, lowercase, number, symbol",
        "Must be at least ${Password.LENGTH_MIN} characters"
    )
}