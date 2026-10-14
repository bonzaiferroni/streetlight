package streetlight.web.view

import koala.model.LabeledRoute
import streetlight.model.ui.StarRoute
import streetlight.web.model.RouteDockState
import kampfire.model.AccountType
import koala.modifier.*
import koala.model.MessageStore
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.component.card
import koala.dom.component.column
import koala.dom.component.navigation
import koala.dom.component.textBlock
import koala.component.bulletsOf
import koala.component.filigree
import koala.component.heading3
import koala.component.span
import streetlight.model.data.Account
import streetlight.model.data.Star
import streetlight.model.ui.StarConfigRoute
import streetlight.web.model.AccountEditor
import streetlight.web.dom.layout.configBody
import streetlight.web.dom.component.configHeading
import streetlight.web.model.dock
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.getAccountEditor

context(model: AccountEditor, star: Star)
fun ViewScope.viewStarConfig() {
    configBody("Star", "Config", "viewStarConfig.kt") {
        configHeading(star.username.value, StarRoute(star.username))

        if (star.accountType == AccountType.Guest) {
            registerAccountForm()
        } else {
            starConfigTabs()
        }
    }

    dock.mergeState(StarConfigRoute, RouteDockState(listOf(LabeledRoute(StarRoute(star.username))), title = star.username.value))
}

fun RouteScope.viewStarConfigRoute() {
    starRouteBlock<StarConfigRoute, Account>(Magic) { account ->
        val model = app.getAccountEditor(account, contentScope)
        context(model) {
            viewStarConfig()
        }
    }
}

context(model: AccountEditor)
fun ViewScope.registerAccountForm() = formColumn {
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
        context(model.passwordEditor) {
            passwordFormSection()
        }
        context(model.emailEditor) {
            emailFormSection()
        }
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