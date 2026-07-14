package streetlight.web.ui

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
import kotlinx.css.GridTemplateColumns
import kotlinx.css.fr
import kotlinx.html.InputType
import streetlight.web.model.EmailEditor
import streetlight.web.model.PasswordEditor
import streetlight.web.model.UserCreatorState

fun AppScope.guestRegistrationForm() {
    val model = app.getUserCreator(parentScope)

    grid(queryTemplate = GridTemplateColumns(1.fr), mod = modify(Gap2)) {
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
    }

    column(modify(QueryContainer)) {
        formPart("A username is all you need to get started.", info = {
            row {
                button("Choose for me", model::generateUsername, modify(Secondary))
            }
        }) {
            formTextField("username", model::setUsername, model.usernameFlow, maxLength = Username.MAX_LENGTH)
        }
        signUpPart({ model.createAccount(AccountType.Guest) })
    }
}

fun AppScope.signUpPart(createAccount: () -> Unit) {
    formPart("To create a Streetlight account, you must be ${UserCreatorState.MINIMUM_AGE} or older.") {
        row(modify(JustifyContentSpaceBetween, AlignItemsCenter)) {
            checkBox("I am ${UserCreatorState.MINIMUM_AGE} or older.")
            button("Register as guest", createAccount, modify(Accent))
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

    form {
        formSection("Username") {
            textField(
                label = null,
                flow = model.usernameFlow,
                onValue = model::setUsername,
                maxLength = Username.MAX_LENGTH,
                placeholder = "Username"
            )
            formBullets("Between ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters.")
        }

        emailFormSection(model.emailEditor)
        passwordFormSection(model.passwordEditor)

        formSection("Requirements") {
            minAgeToggle(model.minAgeField)
            formSubmit(
                buttonText = "Sign up",
                onClick = { model.createAccount(AccountType.Registered) },
                messages = model.messages,
                enabledFlow = model.isValidFlow
            )
        }
    }
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
    // textBlock(
    //     binding = model.isValidFlow,
    //     provideValue = { if (it) "✅" else "❌" }
    // )
}