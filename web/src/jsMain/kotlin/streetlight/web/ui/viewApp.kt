package streetlight.web.ui

import koala.dom.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import revealContent
import streetlight.web.model.createStreetlight
import streetlight.web.pages.AppBodyKey

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
        }

        app.omni.connect()

        // When content is loaded with a hash tag like https://streetlight.ing/#/g/my-galaxy,
        // the home page content will be initially loaded. The opacity is initially 0 to avoid confusion, this
        // reveals the intended content when the script has rendered it.
        delay(100)
        revealContent()
    }
}

