package streetlight.web.ui

import streetlight.model.ui.StarRoute
import streetlight.web.model.RouteDockState
import kampfire.model.AccountType
import koala.LottieFile
import koala.modifier.*
import koala.dom.MessageStore
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.card
import koala.dom.column
import koala.dom.navigation
import koala.dom.textBlock
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.html.span
import streetlight.model.data.Account
import streetlight.model.data.Star
import streetlight.model.ui.StarConfigRoute
import streetlight.web.model.AccountEditor

fun ViewScope.viewStarConfig(star: Star, model: AccountEditor) {
    configBody("Star", "Config", "viewAccountUpdater.kt") {
        introSection("Account", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your account.")
        }

        if (star.accountType == AccountType.Guest) {
            registerAccountForm(model)
        } else {
            starConfigTabs(model)
        }
    }

    dock.mergeState(StarConfigRoute, RouteDockState(listOf(StarRoute(star.username)), title = star.username.value))
}

fun RouteScope.viewStarConfigRoute() {
    starRouteBlock<StarConfigRoute, Account>(Magic) { star, account ->
        val model = app.getAccountEditor(account, contentScope)
        viewStarConfig(star, model)
    }
}

fun ViewScope.registerAccountForm(model: AccountEditor) = formColumn {
    val messages = MessageStore()
    formRow {
        column {
            heading3("Register Account", TextAlignCenter)
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
                Gap(1),
                "The ability to log into your account with other devices",
                "Better account security on shared devices",
                "Avoid automatic deletion after 30 days without activity"
            )
        }
        passwordFormSection(model.passwordEditor)
        emailFormSection(model.emailEditor)
    }

    formSubmit("Register Account", { model.completeRegistration(messages) })
}

private val registerInfo1 = """
You are registered as a guest, which allows you to participate on Streetlight without sharing any information except for your username.
Guest accounts use a secure cookie to authenticate and are automatically deleted after 30 days without activity. 
"""

private val registerInfo2 = """ 
You may continue as a guest and the account will remain available on this device for as long as you are active.
You also have the option to complete the registration process by providing a password.
"""