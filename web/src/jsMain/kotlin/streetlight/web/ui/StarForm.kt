package streetlight.web.ui

import kampfire.model.AccountType
import koala.dom.AppScope
import koala.dom.textEditor
import streetlight.web.model.StarEditor

fun AppScope.starProfileForm(model: StarEditor) = formBodyProto {
    imageFormSection(imageInstructions1, model.imageEditor)
    formCardSection("Content") {
        formPart("The tagline will appear under your name.") {
            formTextField("tagline", model::setTagline, model.taglineFlow, maxLength = 50)
        }
        formPart("The description will appear under the image, before your posts.") {
            textEditor("description", onValue = model::setDescription, flow = model.descriptionFlow)
        }
    }
}

private val imageInstructions1 = "This image will appear at the top of your profile."

fun AppScope.starAccountForm(model: StarEditor) = formBodyProto {
    registerAccountSection(model)
    formCardSection("Identity") {
        formPart("You have the option of sharing your real name.") {
            formTextField("name", model::setName, model.nameFlow, maxLength = 50)
        }
        formPart("") {

        }
    }
}

private fun AppScope.registerAccountSection(model: StarEditor) = if (model.editNow.accountType == AccountType.Guest) {
    formCardSection("Register Account") {
        formPart(registerInfo, registerBullets, "Benefits of registration:") {

        }
    }
} else null

private val registerInfo = """
You are registered as a guest, which allows you to participate on Streetlight without sharing any information except for your username.
Guest accounts are automatically deleted after 30 days without activity. 
You have the option to complete the registration process by providing a password.
"""

private val registerBullets = listOf(
    "The ability to log into your account with other devices",
    "Better account security on shared devices",
    "Avoid automatic deletion after 30 days without activity"
)