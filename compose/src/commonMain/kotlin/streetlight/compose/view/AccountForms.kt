package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import kampfire.api.Password
import kampfire.api.Username
import kampfire.model.AccountType
import koala.SvgFile
import streetlight.compose.model.UserCreator
import streetlight.compose.model.UserCreatorState
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.primaryBg
import streetlight.compose.utility.collectAsState

/**
 * The form to create a guest account: what a guest account is and how it is kept, the username, and the age
 * requirement. The cousin of the web's `guestRegistrationForm`.
 */
@Composable
context(model: UserCreator)
fun GuestRegistrationForm() {
    val theme = currentTheme
    val isValid by model.isValidState.collectAsState()

    Column(2) {
        ColumnRow {
            FormSection(null) {
                BasicText(
                    "Guest accounts",
                    Modifier.fillMaxWidth(),
                    style = theme.text.heading3.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
                BasicText(GuestIntro, style = theme.text.paragraph, color = { theme.colors.ink })
                BasicText(GuestLifetime, style = theme.text.paragraph, color = { theme.colors.ink })
            }
            FormSection(null) {
                Column(1, Modifier.borderRadius(2, moonShadow = true, outline = Outline.Mid).primaryBg().padding(2)) {
                    BasicText(
                        "How it works",
                        Modifier.fillMaxWidth().filigree().opacityHigh(),
                        style = theme.text.regular.copy(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
                        color = { theme.colors.ink },
                    )
                    FormBullets(
                        "The guest's credentials are kept on this device, so the account is only available here.",
                        "Removing the app's data removes access to the account.",
                        "Anyone else with access to this device can also control the account.",
                    )
                }
            }
            UsernameSection()
            RequirementsSection()
        }
        FormSubmit("Register as Guest", { model.createAccount(AccountType.Guest) }, model.messages, isValid)
    }
}

/**
 * The form to create a registered account: the username, an optional email, the password and its confirmation, and
 * the age requirement. The cousin of the web's `fullRegistrationForm`.
 */
@Composable
context(model: UserCreator)
fun FullRegistrationForm() {
    val isValid by model.isValidState.collectAsState()

    Column(2) {
        ColumnRow {
            UsernameSection()
            FormSection("Email") {
                TextField(model.emailField, "email", icon = SvgFile.Mail)
                FormBullets(
                    "Providing an email address is optional",
                    "It can be used to reset your password",
                    "Streetlight will never share your email or contact you without your request",
                )
            }
            FormSection("Password") {
                TextField(model.passwordField, "password", icon = SvgFile.Key, isPassword = true)
                TextField(model.confirmationField, "confirm password", icon = SvgFile.Key, isPassword = true)
                FormBullets(
                    "Must have at least 3: uppercase, lowercase, number, symbol",
                    "Must be at least ${Password.LENGTH_MIN} characters",
                )
            }
            RequirementsSection()
        }
        FormSubmit("Sign up", { model.createAccount(AccountType.Registered) }, model.messages, isValid)
    }
}

/** The username's field beside the button that chooses one, and its length. */
@Composable
context(model: UserCreator)
private fun FlowRowScope.UsernameSection() = FormSection("Username") {
    Row(1, verticalAlignment = Alignment.CenterVertically) {
        TextField(model.usernameField, "username", Modifier.weight(1f), icon = SvgFile.User)
        Button("Choose for me", onClick = model::generateUsername, style = ButtonStyle.Secondary)
    }
    FormBullets("Between ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters.")
}

/** The age an account requires, and the box that confirms it. */
@Composable
context(model: UserCreator)
private fun FlowRowScope.RequirementsSection() = FormSection("Requirements") {
    val theme = currentTheme
    Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        BasicText(
            "To create an account, you must be ${UserCreatorState.MINIMUM_AGE} or older.",
            style = theme.text.regular.copy(textAlign = TextAlign.Center),
            color = { theme.colors.ink },
        )
        CheckBox(model.minAgeField, "I am ${UserCreatorState.MINIMUM_AGE} or older.")
    }
}

private const val GuestIntro =
    "Guest accounts are temporary and make it easy to participate in Streetlight events and submit content."

private const val GuestLifetime =
    "Guest accounts and user data are automatically deleted after 30 days without activity. If you decide to stick " +
        "around, you can make the account permanent and more secure by adding a password."
