package streetlight.web.ui

import kampfire.model.AccountType
import kampfire.model.PrivateInfo
import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.IdentityInfo
import streetlight.model.data.Star
import streetlight.model.data.StarEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.StarConfigRoute
import streetlight.web.model.StarEditor

fun ViewScope.viewStarConfig(model: StarEditor) {
    column(mod = BodyStyle.Mod) {
        introSection("User Settings", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your account and profile.")
        }

        tabs {
            if (model.stateNow.edit.accountType == AccountType.Guest) {
                tab("registration") {
                    registerAccountForm(model)
                }
            }

            tab("profile") {
                starProfileForm(model)
            }

            tab("account") {
                starAccountForm(model)
            }
        }

        appFooter("")
    }
}

fun RouteScope.viewStarConfigRoute() {
    routeBlock<StarConfigRoute, IdentityInfo> { identityInfo ->
        starGate { star ->
            val model = app.getStarEditor(star.toEdit(identityInfo), contentScope)
            viewStarConfig(model)
        }
    }
}

