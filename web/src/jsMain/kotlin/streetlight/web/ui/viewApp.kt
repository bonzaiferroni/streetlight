package streetlight.web.ui

import kampfire.model.Token
import koala.core.addGlobalFunctions
import koala.css.KoalaBody
import koala.css.Property
import koala.dom.*
import koala.model.Portal
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.css.Display
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.koinApplication
import streetlight.web.io.OmniLog
import streetlight.web.layouts.LightControl
import streetlight.web.model.TransitMap
import streetlight.web.model.StarSession
import koala.utils.launch
import streetlight.model.ui.VerifyEmailRoute

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    // val app = createStreetlight(scope)

    val koin = koinApplication {
        modules(appModule)
    }.koin

    println(VerifyEmailRoute(Token("mytoken")).toAbsolutePath())

    val app = AppContainer(koin)
    val lightService = koin.get<LightService>()

    window.addGlobalFunctions(globalFunExtended)
    window.addGlobalFunctions(listOf(
        LightControl.ToggleFun to lightService::toggleLight
    ))

    with (app) {
        val scope: CoroutineScope = get()
        val gate: StarSession = get()
        val omni: OmniLog = get()
        val transit: TransitMap = get()
        val portal: Portal = get()
        transit.init()

        scope.launch(::viewApp) {

            try {
                // signs in user if configured
                gate.readUser(null)

                mountRootView(KoalaBody.PortalMount, scope, app) {
                    // renders routes from portal.routeFlow
                    viewPortal()
                    // shows user badge in upper right corner
                    wireBadge()
                    // shows content in user menu
                    queryAndWireStarHelm()

                    // td: reimplement as sidebar option
                    // wireRightPanel()
                    wireToaster()

                    // hides the element that holds server rendered content
                    val shellBox = document.getElementById(KoalaBody.ShellMount)
                    shellBox.setStyle(Property.Display.to(Display.none))
                }

                // td: reimplement as sidebar option
                // omni.connect()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                portal.notifyWrecked()
                throw e
            }
        }
    }
}

