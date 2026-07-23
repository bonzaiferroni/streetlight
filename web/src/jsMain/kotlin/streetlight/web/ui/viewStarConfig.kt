package streetlight.web.ui

import kampfire.model.AccountType
import kampfire.model.PrivateInfo
import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.MenuRoute
import koala.dom.RouteScope
import koala.dom.TagScope
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.routeMenu
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.toMenuRoute
import streetlight.model.data.IdentityInfo
import streetlight.model.data.Star
import streetlight.model.data.StarEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.StarConfigRoute
import streetlight.web.model.StarEditor
import streetlight.web.shells.starRouteMenu

fun ViewScope.viewStarConfig(star: Star, model: StarEditor) {
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

        starRouteMenu(star, StarConfigRoute, true)
    }
}

fun RouteScope.viewStarConfigRoute() {
    routeBlock<StarConfigRoute, IdentityInfo> { identityInfo ->
        starGate { star ->
            val model = app.getStarEditor(star.toEdit(identityInfo), contentScope)
            viewStarConfig(star, model)
        }
    }
}
