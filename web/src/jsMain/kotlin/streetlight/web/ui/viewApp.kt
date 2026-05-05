package streetlight.web.ui

import koala.dom.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import streetlight.model.data.GalaxyId
import streetlight.model.data.PostId
import streetlight.web.layouts.PostKey
import streetlight.web.model.createStreetlight
import streetlight.web.pages.AppBodyKey
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    val scope = MainScope()
    val app = createStreetlight(scope)

    scope.launch {
        // signs in user if configured
        app.gate.readUser()

        // hides the element that holds server rendered content
        val shellBox = document.getElementById(AppBodyKey.ShellBoxId)
        shellBox.style.display = "none" // td: use pointer-events: none

        val portalMount = document.getElementById(AppBodyKey.PortalMountId)

        portalMount.replaceRender(app.appScope) {
            // renders routes from portal.routeFlow
            appNavigation(app)
            // shows user badge in upper right corner
            wireBadge(app)
            // shows content in user menu
            queryAndWireStarHelm(app)

            wireRightPanel(app)
            wireToaster(app)

            viewContextOf(app) {
                registerMenu(PostKey.PostMenuId, { PostId(it) }, AppContext::postMenu)
            }
        }

        try {
            app.omni.connect()
        } catch (e: Exception) {
            console.log("unable to connect to omni log:\n${e.message}")
        }
    }
}

