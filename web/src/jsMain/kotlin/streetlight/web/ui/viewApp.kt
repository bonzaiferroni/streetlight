package streetlight.web.ui

import koala.css.KoalaBody
import koala.css.Property
import koala.dom.*
import koala.interop.KtFunction
import koala.interop.addGlobalFunctions
import koala.model.Portal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.css.Display
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.koinApplication
import streetlight.web.io.OmniClient
import streetlight.web.layouts.LightControl
import streetlight.web.model.TransitMap
import streetlight.web.model.SessionGate
import koala.utils.launch
import web.dom.document

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    // val app = createStreetlight(scope)

    val koin = koinApplication {
        modules(appModule)
    }.koin

    val app = AppContainer(koin)
    val lightService = koin.get<LightService>()

    addGlobalFunctions(KtFunction(LightControl.ToggleFun, lightService::toggleLight))

    with (app) {
        val scope: CoroutineScope = get()
        val gate: SessionGate = get()
        val omni: OmniClient = get()
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
                    queryAndWireStarNav()
                    wireStarMenu()
                    wireCurator()
                    wireCreatePost()
                    wirePostMenu()
                    wireMessageDialog()
                    wireFps()

                    // td: reimplement as sidebar option
                    // wireRightPanel()
                    wireToaster()
                    wireOmni(omni)

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

