package streetlight.web.ui

import kampfire.model.AccountType
import koala.LottieFile
import koala.css.AlignSelfStart
import koala.css.Gap1
import koala.css.Italic
import koala.css.OpacityHigh
import koala.css.PrimaryCardBg
import koala.css.TextAlignCenter
import koala.css.modify
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.card
import koala.dom.column
import koala.dom.navigation
import koala.dom.routeBlock
import koala.dom.tabs
import koala.dom.textBlock
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.html.span
import streetlight.model.data.Account
import streetlight.model.data.Star
import streetlight.model.ui.UpdateAccountRoute
import streetlight.web.model.AccountEditor
import streetlight.web.shells.starRouteMenu

fun ViewScope.viewAccountUpdater(star: Star, model: AccountEditor) {
    column(mod = BodyStyle.Column) {
        introSection("Account", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your account.")
        }

        tabs {
            if (star.accountType == AccountType.Guest) {
                tab("registration") {
                    registerAccountForm(model)
                }
            }

            tab("account") {
                starAccountForm(model)
            }
        }

        appFooter("")

        starRouteMenu(star, UpdateAccountRoute, true)
    }
}

fun RouteScope.viewUpdateAccountRoute() {
    routeBlock<UpdateAccountRoute, Account> { account ->
        starGate { star ->
            val model = app.getAccountEditor(account, contentScope)
            viewAccountUpdater(star, model)
        }
    }
}

fun ViewScope.registerAccountForm(model: AccountEditor) = form {
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