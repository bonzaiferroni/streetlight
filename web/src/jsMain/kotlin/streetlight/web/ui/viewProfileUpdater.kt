package streetlight.web.ui

import koala.LottieFile
import koala.dom.RouteScope
import koala.dom.ViewScope
import koala.dom.column
import koala.dom.textBlock
import streetlight.model.data.Star
import streetlight.model.data.toEdit
import streetlight.model.ui.UpdateProfileRoute
import streetlight.web.model.ProfileEditor
import streetlight.web.shells.starRouteMenu

fun ViewScope.viewProfileUpdater(star: Star, model: ProfileEditor) {
    column(mod = BodyStyle.Column) {
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
