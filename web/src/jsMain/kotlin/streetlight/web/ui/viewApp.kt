package streetlight.web.ui

import koala.dom.*
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import revealContent
import streetlight.web.model.ThemeReactor
import streetlight.web.model.createStreetlight
import streetlight.web.pages.AppBodyKey

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    val scope = MainScope()
    val app = createStreetlight(scope)

    ThemeReactor(scope, app.config)

    scope.launch {
        app.gate.readUser()

        val shellBox = document.getElementById(AppBodyKey.ShellBoxId)
        shellBox.style.display = "none" // td: use pointer-events: none

        val portalMount = document.getElementById(AppBodyKey.PortalMountId)

        portalMount.renderRoot(app.appScope) {
            appNavigation(app)
            wireBadge(app)
            queryAndWireStarHelm(app)
        }

        // When content is loaded with a hash tag like https://streetlight.ing/#/g/my-galaxy,
        // the home page content will be initially loaded. The opacity is initially 0 to avoid confusion, this
        // reveals the intended content when the script has rendered it.
        delay(100)
        revealContent()
    }
}

