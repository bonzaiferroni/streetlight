package streetlight.compose.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import kampfire.api.EmailAddress
import kampfire.api.Password
import kampfire.api.obfuscate
import kampfire.model.AccountType
import koala.SvgFile
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.AccountEditor
import streetlight.compose.model.StarConfigRoute
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.primaryBg
import streetlight.compose.utility.collectAsState
import streetlight.model.data.Account
import streetlight.model.data.EmailStatus
import streetlight.model.data.Star
import streetlight.model.data.viableEmail

/**
 * The screen of [StarConfigRoute]: the logo as its top bar, and, while a star is signed in, the star's account as its
 * top bar, and their account once read, shown in the [StarConfigView].
 */
@Composable
fun StarConfigScreen() {
    val scrollState = rememberScrollState()
    TopBarMount(TopBar.Logo, scrollState)
    StarGate {
        // the account is the signed-in star's, so another star's sign-in reads and edits their own
        val starKey = contextOf<Star>().username.value
        TopBarMount(TopBar.TwoTone("$starKey's ", "Account"), scrollState)
        Inflated<Account>(StarConfigRoute, key = starKey) { account ->
            val editor = koinViewModel<AccountEditor>(key = starKey) { parametersOf(account) }
            context(editor) { StarConfigView(scrollState) }
        }
    }
}

/**
 * The view of [StarConfigRoute], in a column scrolled by [scrollState] under the top bar that names it: the form to
 * register a guest's account, or the email and password of a registered one. The cousin of the web's
 * `viewStarConfig`.
 */
@Composable
context(model: AccountEditor, star: Star)
fun StarConfigView(scrollState: ScrollState) {
    ConfigBody(null, scrollState) {
        when (star.accountType) {
            AccountType.Guest -> RegisterAccountForm()
            AccountType.Registered -> ColumnRow {
                EmailSection()
                PasswordSection()
            }
        }
    }
}

/**
 * The form to make a guest's account a registered one: what registering gives, a password, and an optional email. The
 * cousin of the web's `registerAccountForm`.
 */
@Composable
context(model: AccountEditor)
private fun RegisterAccountForm() {
    val theme = currentTheme
    val message by model.registerMessageState.collectAsState()

    Column(2) {
        ColumnRow {
            FormSection(null) {
                BasicText(
                    "Register Account",
                    Modifier.fillMaxWidth(),
                    style = theme.text.heading3.copy(textAlign = TextAlign.Center),
                    color = { theme.colors.ink },
                )
                BasicText(RegisterIntro, style = theme.text.paragraph, color = { theme.colors.ink })
                BasicText(RegisterChoice, style = theme.text.paragraph, color = { theme.colors.ink })
            }
            FormSection(null) {
                Column(1, Modifier.borderRadius(2, moonShadow = true, outline = Outline.Mid).primaryBg().padding(2)) {
                    BasicText(
                        "Benefits of Registration",
                        Modifier.fillMaxWidth().filigree().opacityHigh(),
                        style = theme.text.regular.copy(fontStyle = FontStyle.Italic, textAlign = TextAlign.Center),
                        color = { theme.colors.ink },
                    )
                    FormBullets(
                        "The ability to sign in to your account on other devices",
                        "Better account security on shared devices",
                        "Avoid automatic deletion after 30 days without activity",
                    )
                }
            }
            FormSection("Password") { NewPasswordFields() }
            FormSection("Email") { NewEmailField() }
        }
        FormSubmit("Register Account", model::completeRegistration, message)
    }
}

/**
 * The account's email: the address and its status with the ways to change it, the field to add one, a check before
 * removing it, or, when mail to it could not be delivered, the field to give another. The cousin of the web's
 * `changeEmailForm`.
 */
@Composable
context(model: AccountEditor)
private fun FlowRowScope.EmailSection() = FormSection("Email") {
    val account by model.accountState.collectAsState()
    val isRemoving by model.isRemovingEmailField.collectAsState()
    val email = account.email
    val status = account.emailStatus

    when {
        status == EmailStatus.NotOwned || status == EmailStatus.Bounced -> UndeliveredEmailForm(email)
        email == null -> AddEmailForm()
        isRemoving -> RemoveEmailForm()
        else -> EmailProvided(email, status)
    }
}

/** The account's [email] with its [status], and the buttons to remove it, change it, or verify it. */
@Composable
context(model: AccountEditor)
private fun EmailProvided(email: EmailAddress, status: EmailStatus?) {
    val theme = currentTheme
    val isSent by model.isVerifySentState.collectAsState()
    val message by model.emailMessageState.collectAsState()
    val isVerified = status == EmailStatus.Verified

    Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(1, verticalAlignment = Alignment.CenterVertically) {
            BasicText(email.obfuscate(), style = theme.text.regular, color = { theme.colors.ink })
            BasicText(
                if (isVerified) "(Verified)" else "(Unverified)",
                style = theme.text.regular,
                color = { if (isVerified) theme.colors.greenFg else theme.colors.yellowFg },
            )
        }
        Row(1) {
            Button("Remove", onClick = { model.isRemovingEmailField.set(true) }, style = ButtonStyle.Zen)
            Button("Change", onClick = model::changeEmail, style = ButtonStyle.Zen)
            if (!isVerified) {
                when (isSent) {
                    true -> Button("Resend Email", onClick = model::verifyExistingEmail, style = ButtonStyle.Zen)
                    false -> Button("Verify", onClick = model::verifyExistingEmail)
                }
            }
        }
        FormMessage(message)
    }
}

/** The field to add an email, or to change it, confirmed by the current password when the old one was verified. */
@Composable
context(model: AccountEditor)
private fun AddEmailForm() {
    val isChanging by model.isChangingEmailState.collectAsState()
    val hasVerifiedEmail by model.hasVerifiedEmailState.collectAsState()
    val message by model.emailMessageState.collectAsState()

    Column(1) {
        if (isChanging && hasVerifiedEmail) VerifyPasswordField("Please provide a password to authorize change.")
        NewEmailField()
        FormSubmit(if (isChanging) "change email" else "add email", model::addEmail, message)
    }
}

/** The check before the email is removed, by the current password when the email is verified. */
@Composable
context(model: AccountEditor)
private fun RemoveEmailForm() {
    val hasVerifiedEmail by model.hasVerifiedEmailState.collectAsState()
    val message by model.emailMessageState.collectAsState()

    Column(1) {
        when (hasVerifiedEmail) {
            true -> VerifyPasswordField("Please provide a password to authorize removal.")
            false -> CenteredText("Please confirm that you want to remove the email address from this account.")
        }
        FormSubmit("Remove email", model::removeEmail, message)
        Button("back", onClick = { model.isRemovingEmailField.set(false) }, style = ButtonStyle.Zen)
    }
}

/** The field to give another email, after mail to [email] could not be delivered. */
@Composable
context(model: AccountEditor)
private fun UndeliveredEmailForm(email: EmailAddress?) {
    val theme = currentTheme
    val message by model.emailMessageState.collectAsState()

    Column(1) {
        BasicText(
            "We were unable to deliver to the email address you provided, please provide a different one.",
            style = theme.text.regular,
            color = { theme.colors.yellowFg },
        )
        email?.let { CenteredText("Provided address: ${it.obfuscate()}") }
        NewEmailField()
        FormSubmit("add email", model::addEmail, message)
    }
}

/**
 * The account's password: the button to change it, or the new password and its confirmation, checked by the current
 * password when the email is viable, which can also send a link to reset it. The cousin of the web's
 * `changePasswordForm`.
 */
@Composable
context(model: AccountEditor)
private fun FlowRowScope.PasswordSection() = FormSection("Password") {
    val isEditing by model.isEditingPasswordField.collectAsState()
    val message by model.passwordMessageState.collectAsState()
    val account by model.accountState.collectAsState()

    when (isEditing) {
        false -> Column(1, Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button("change password", onClick = { model.isEditingPasswordField.set(true) })
            FormMessage(message)
        }
        true -> Column(1) {
            val viableEmail = account.viableEmail
            if (viableEmail != null) VerifyPasswordField("Please provide your current password to authorize change.")
            NewPasswordFields()
            if (viableEmail != null) {
                Button("I forgot my current password", onClick = model::resetPassword, style = ButtonStyle.Zen)
            }
            FormSubmit("change", model::changePassword, message)
            Button("back", onClick = { model.isEditingPasswordField.set(false) }, style = ButtonStyle.Zen)
        }
    }
}

/** The current password, under [note] on why it is asked for. */
@Composable
context(model: AccountEditor)
private fun VerifyPasswordField(note: String) {
    CenteredText(note)
    TextField(model.verifyPasswordField, "current password", icon = SvgFile.Key, isPassword = true)
}

/** The new password and its confirmation, and what a password must have. */
@Composable
context(model: AccountEditor)
private fun NewPasswordFields() {
    TextField(model.passwordField, "password", icon = SvgFile.Key, isPassword = true)
    TextField(model.confirmationField, "confirm password", icon = SvgFile.Key, isPassword = true)
    FormBullets(
        "Must have at least 3: uppercase, lowercase, number, symbol",
        "Must be at least ${Password.LENGTH_MIN} characters",
    )
}

/** The field of a new email, and what the email is for. */
@Composable
context(model: AccountEditor)
private fun NewEmailField() {
    TextField(model.emailField, "email", icon = SvgFile.Mail)
    FormBullets(
        "Providing an email address is optional",
        "It can be used to reset your password",
        "Streetlight will never share your email or contact you without your request",
    )
}

/** [text] centered across its column. */
@Composable
private fun CenteredText(text: String) {
    val theme = currentTheme
    BasicText(
        text,
        Modifier.fillMaxWidth(),
        style = theme.text.regular.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}

/** A form's [message], if any, centered. */
@Composable
private fun FormMessage(message: String?) {
    message?.let { CenteredText(it) }
}

private const val RegisterIntro =
    "You are registered as a guest, which allows you to participate on Streetlight without sharing any information " +
        "except for your username. Guest accounts are kept on this device and are automatically deleted after 30 " +
        "days without activity."

private const val RegisterChoice =
    "You may continue as a guest and the account will remain available on this device for as long as you are " +
        "active. You also have the option to complete the registration process by providing a password."
