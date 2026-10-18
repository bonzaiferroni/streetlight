package streetlight.web.view

import kampfire.api.Username
import kampfire.model.AccountType
import koala.modifier.*
import koala.dom.*
import koala.component.bulletsOf
import koala.component.filigree
import koala.component.heading3
import koala.component.span
import kampfire.model.MutableTap
import streetlight.web.model.UserCreator
import streetlight.web.model.UserCreatorState
import koala.dom.component.*
import streetlight.web.dom.layout.formBullets
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

context(model: UserCreator)
fun ViewScope.guestRegistrationForm() {
    formColumn {
        formRow {
            column {
                heading3("Guest accounts", TextAlignCenter)
                textBlock(guestAccountIntro1)
                textBlock {
                    span(guestAccountIntro2, Flex1)
                    navigation { +"→ Learn More" }
                }
            }
            card(modify(PrimaryCardBg, Outline, MoonShadow)) {
                filigree {
                    textBlock("How it works", modify(OpacityHigh, Italic))
                }
                bulletsOf(
                    Gap(1),
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
            usernameSection()
            requirementsSection()
        }
        formSubmit(
            label = "Register as Guest",
            onClick = { model.createAccount(AccountType.Guest) },
            messenger = model.messages,
            enabledTap = model.isValidField
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

context(model: UserCreator)
fun ViewScope.fullRegistrationForm() {

    formRow {
        usernameSection()

        context(model.emailEditor) {
            emailFormSection()
        }
        context(model.passwordEditor) {
            passwordFormSection()
        }

        requirementsSection()
    }
}

context(model: UserCreator)
private fun ViewScope.requirementsSection() = formSection("Requirements") {
    minAgeToggle(model.minAgeField)
}

context(model: UserCreator)
private fun ViewScope.usernameSection() = formSection("Username") {
    row {
        textField(
            label = null,
            mod = Flex1,
            field = model.usernameField,
            maxLength = Username.MAX_LENGTH,
            placeholder = "Username"
        )
        button("Choose for me", model::generateUsername, Secondary)
    }
    formBullets(null, "Between ${Username.MIN_LENGTH} and ${Username.MAX_LENGTH} characters.")
}

fun ViewScope.minAgeToggle(
    field: MutableTap<Boolean>
) = column(AlignItemsCenter) {
    textBlock("To create an account, you must be 17 or older.", TextAlignCenter)
    checkBox(field, "I am ${UserCreatorState.MINIMUM_AGE} or older.")
}
