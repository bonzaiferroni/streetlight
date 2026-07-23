package streetlight.web.ui

import koala.css.AlignSelfStart
import koala.css.Gap1
import koala.css.Italic
import koala.css.OpacityHigh
import koala.css.PrimaryCardBg
import koala.css.TextAlignCenter
import koala.css.modify
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.html.span
import streetlight.web.model.StarEditor

fun ViewScope.starProfileForm(model: StarEditor) = form {
    formRow {
        imageFormSection(imageInstructions1, model.imageEditor)
        formSection("Content") {
            textField(model.taglineField, "tagline", maxLength = 50)
            formText("The tagline will appear under your name.")
            textEditor(model.descriptionField, "description")
            formText("The description will appear under the image, before your posts.")
        }
    }
    formSubmit("Save", model::submit, model.messages)
}

private val imageInstructions1 = "This image will appear at the top of your profile."

fun ViewScope.registerAccountForm(model: StarEditor) = form {
    formRow {
        column {
            heading3("Register Account", modify(TextAlignCenter))
            textBlock(registerInfo1)
            textBlock {
                span(registerInfo2)
                navigation { +"→ Learn about the difference" }
            }
        }
        card(modify(PrimaryCardBg, AlignSelfStart)) {
            filigree {
                textBlock("Benefits of Registration", modify(OpacityHigh, Italic))
            }
            bulletsOf(
                modify(Gap1),
                "The ability to log into your account with other devices",
                "Better account security on shared devices",
                "Avoid automatic deletion after 30 days without activity"
            )
        }
        passwordFormSection(model.passwordEditor)
        emailFormSection(model.emailEditor)
    }

    formSubmit("Register Account", model::completeRegistration)
}

private val registerInfo1 = """
You are registered as a guest, which allows you to participate on Streetlight without sharing any information except for your username.
Guest accounts use a secure cookie to authenticate and are automatically deleted after 30 days without activity. 
"""

private val registerInfo2 = """ 
You may continue as a guest and the account will remain available on this device for as long as you are active.
You also have the option to complete the registration process by providing a password.
"""