package streetlight.web.ui

import kampfire.model.AccountType
import koala.LottieFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.Account
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.UpdateAccountRoute
import streetlight.model.ui.UpdateProfileRoute
import streetlight.web.model.AccountEditor
import streetlight.web.shells.starRouteMenu

fun ViewScope.viewAccountUpdater(star: Star, model: AccountEditor) {
    column(mod = BodyStyle.Mod) {
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