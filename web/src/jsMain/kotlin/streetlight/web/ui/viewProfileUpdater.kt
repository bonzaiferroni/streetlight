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
import streetlight.model.ui.UpdateProfileRoute
import streetlight.web.model.AccountEditor
import streetlight.web.model.ProfileEditor
import streetlight.web.shells.starRouteMenu

fun ViewScope.viewProfileUpdater(star: Star, model: ProfileEditor) {
    column(mod = BodyStyle.Mod) {
        introSection("Update Profile", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your profile.")
        }

        starProfileForm(model)

        appFooter("")

        starRouteMenu(star, UpdateProfileRoute, true)
    }
}

fun RouteScope.viewUpdateProfileRoute() {
    starGate { star ->
        val model = app.getProfileEditor(star.toEdit(), contentScope)
        viewProfileUpdater(star, model)
    }
}
