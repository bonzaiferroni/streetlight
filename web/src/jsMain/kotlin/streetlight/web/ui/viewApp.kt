package streetlight.web.ui

import kampfire.api.toSlug
import koala.core.addGlobalFunctions
import koala.dom.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import streetlight.web.layouts.PostKey
import streetlight.web.pages.AppBodyKey
import org.koin.dsl.koinApplication
import streetlight.web.io.OmniLog
import streetlight.web.layouts.LightControl
import streetlight.web.model.TransitMap
import streetlight.web.model.UserGate

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    // val app = createStreetlight(scope)

    val koin = koinApplication {
        modules(appModule)
    }.koin

    val app = AppContainer(koin)
    val lightService = koin.get<LightService>()

    window.addGlobalFunctions(globalFunExtended)
    window.addGlobalFunctions(listOf(
        LightControl.ToggleFun to lightService::toggleLight
    ))

    with (app) {
        val scope: CoroutineScope = get()
        val gate: UserGate = get()
        val omni: OmniLog = get()
        val transit: TransitMap = get()
        transit.init()

        scope.launch {
            // signs in user if configured
            gate.readUser()

            // hides the element that holds server rendered content
            val shellBox = document.getElementById(AppBodyKey.ShellBoxId)
            shellBox.style.display = "none" // td: use pointer-events: none

            val portalMount = document.getElementById(AppBodyKey.PortalMountId)

            portalMount.renderRoot(scope, app) {
                // renders routes from portal.routeFlow
                appNavigation()
                // shows user badge in upper right corner
                wireBadge()
                // shows content in user menu
                queryAndWireStarHelm()

                wireRightPanel()
                wireToaster()

                registerMenu(PostKey.PostMenuId, { it.toSlug() }, RenderScope::postMenu)
            }

            try {
                omni.connect()
            } catch (e: Exception) {
                console.log("unable to connect to omni log:\n${e.message}")
            }
        }
    }
}

