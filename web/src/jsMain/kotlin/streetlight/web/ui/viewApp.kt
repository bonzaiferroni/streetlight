package streetlight.web.ui

import koala.css.DisplayNone
import koala.css.KoalaBody
import koala.dom.*
import koala.interop.addGlobalFunctions
import koala.model.Portal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.koinApplication
import streetlight.web.io.OmniClient
import streetlight.web.model.TransitMap
import streetlight.web.model.SessionGate
import koala.utils.launch
import streetlight.web.interop.appGlobalFunctions
import streetlight.web.pages.AppOverlay
import web.dom.document

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    // val app = createStreetlight(scope)

    val koin = koinApplication {
        modules(appModule)
    }.koin

    val app = AppContainer(koin)

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
                    wireStarPanel()
                    wireStarMenu()
                    wireCuratorMenu()
                    wireCreatePost()
                    wirePostMenu()
                    wireMessageDialog()
                    wireGalaxyMenu()
                    wireFps()
                    wireSignInDialog()
                    addGlobalFunctions(appGlobalFunctions())

                    // td: reimplement as sidebar option
                    // wireRightPanel()
                    wireToaster()
                    wireOmni(omni)

                    // hides the element that holds server rendered content
                    document.getElementById(KoalaBody.ShellMount).modify(DisplayNone)
                    document.getElementById(AppOverlay.WorkSignal).modify(DisplayNone)
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

