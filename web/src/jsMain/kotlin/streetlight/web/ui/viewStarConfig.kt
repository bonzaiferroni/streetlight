package streetlight.web.ui

import kampfire.model.handleResponse
import koala.LottieFile
import koala.dom.AppScope
import koala.dom.column
import koala.dom.routeBlock
import koala.dom.tab
import koala.dom.tabs
import koala.dom.textBlock
import streetlight.model.data.StarEdit
import streetlight.model.data.toEdit
import streetlight.model.ui.StarConfigRoute
import streetlight.web.model.StarEditor

fun AppScope.viewStarConfig(model: StarEditor) {
    column {
        introSection("User Settings", lottie = LottieFile.ServerSync) {
            textBlock("Here you can make changes to your account and profile.")
        }

        tabs {
            tab("profile") {
                starProfileForm(model)
            }

            tab("account") {
                starAccountForm(model)
            }
        }
    }
}

fun AppScope.viewStarConfigRoute() {
    starGate { star ->
        routeBlock<StarConfigRoute, StarEdit>(portal, { route ->
            val info = api.readIdentityInfo().handleResponse(toaster) ?: return@routeBlock null
            star.toEdit(info)
        }) { edit ->
            val model = app.getStarEditor(edit, parentScope)
        }
    }
}

