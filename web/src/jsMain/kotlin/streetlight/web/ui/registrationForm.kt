package streetlight.web.ui

import kampfire.api.Password
import kampfire.api.Username
import kampfire.model.AccountType
import koala.css.Accent
import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.html.span
import koala.model.StateField
import kotlinx.html.InputType
import streetlight.web.model.EmailEditor
import streetlight.web.model.PasswordEditor
import streetlight.web.model.UserCreator
import streetlight.web.model.UserCreatorState

fun AppScope.guestRegistrationForm(model: UserCreator) {
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
                    { textBlock("Once you sign out or clear your browser's cookies, you won't be able to sign in again with the same identity.") },
                    { textBlock("Guest accounts are automatically deleted after 30 days without activity.") },
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
            enabledFlow = model.isValidFlow
        )
    }
}

val guestAccountIntro1 = """
Guest accounts are temporary and make it easy to participate in Streetlight events and submit content. 
"""
val guestAccountIntro2 =
    "If you decide to stick around, you can make the account permanent and more secure by adding a password. "

fun AppScope.fullRegistrationForm(model: UserCreator) {

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
            enabledFlow = model.isValidFlow
        )
    }
}

private fun AppScope.requirementsSection(model: UserCreator) = formSection("Requirements") {
    minAgeToggle(model.minAgeField)
}

private fun AppScope.usernameSection(model: UserCreator) = formSection("Username") {
    row {
        textField(
            label = null,
            mod = modify(Flex1),
            flow = model.usernameFlow,
            onValue = model::setUsername,
            maxLength = Username.MAX_LENGTH,
            placeholder = "Username"
        )
        button("Choose for me", model::generateUsername, modify(Secondary))
    }
    formBullets("Between ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters.")
}

fun AppScope.minAgeToggle(
    field: StateField<Boolean>
) = column(modify(AlignItemsCenter)) {
    textBlock("To create an account you must be 17 or older.", modify(TextAlignCenter))
    checkBox("I am ${UserCreatorState.MINIMUM_AGE} or older.", field.onValue, field.flow)
}

fun AppScope.emailFormSection(model: EmailEditor) = formSection("Email") {
    textField("optional", model::setEmail, model.emailFlow, placeholder = "email")
    formBullets(
        "Providing an email address is optional",
        "It can be used to reset your password",
        "Streetlight will never share your email or contact you without your request"
    )
}

// formTextField("email", model::setEmail, model.emailFlow)



fun AppScope.passwordFormSection(model: PasswordEditor) = formSection("Password") {
    textField(
        label = "password",
        flow = model.passwordFlow,
        onValue = model::setPassword
    ) {
        type = InputType.password
    }
    textField(
        label = "confirm password",
        flow = model.confirmationFlow,
        onValue = model::setConfirmation
    ) {
        type = InputType.password
    }
    formBullets(
        "Must have at least 3: uppercase, lowercase, number, symbol",
        "Must be at least ${Password.LENGTH_MIN} characters"
    )
}