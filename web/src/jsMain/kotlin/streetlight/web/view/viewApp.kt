package streetlight.web.view

import koala.modifier.DisplayNone
import koala.modifier.KoalaBody
import koala.dom.*
import koala.interop.addGlobalFunctions
import koala.model.Portal
import koala.modifier.modify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.dsl.koinApplication
import streetlight.web.io.OmniClient
import streetlight.web.model.TransitMap
import streetlight.web.model.SessionClient
import koala.utility.launch
import streetlight.web.interop.appGlobalFunctions
import streetlight.web.page.AppOverlay
import web.dom.document
import streetlight.web.model.appModule
import streetlight.web.utility.element
import streetlight.web.model.portal
import streetlight.web.interop.wireBadge
import streetlight.web.interop.wireCreatePost
import streetlight.web.interop.wireCuratorMenu
import streetlight.web.interop.wireEntityDialog
import streetlight.web.interop.wireFps
import streetlight.web.interop.wireGalaxyMenu
import streetlight.web.interop.wireMessageDialog
import streetlight.web.interop.wireOmni
import streetlight.web.interop.wirePostMenu
import streetlight.web.interop.wireRightPanel
import streetlight.web.interop.wireSignInDialog
import streetlight.web.interop.wireStarHelm
import streetlight.web.interop.wireStarMenu
import streetlight.web.interop.wireTagFilterMenu
import streetlight.web.interop.wireToaster

@OptIn(ExperimentalSerializationApi::class)
fun viewApp() {
    // val app = createStreetlight(scope)

    val koin = koinApplication {
        modules(appModule)
    }.koin

    val app = AppContainer(koin)

    with (app) {
        val scope: CoroutineScope = get()
        val gate: SessionClient = get()
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
                    wireStarHelm()
                    wireStarMenu()
                    wireCuratorMenu()
                    wireCreatePost()
                    wirePostMenu()
                    wireTagFilterMenu()
                    wireMessageDialog()
                    wireEntityDialog()
                    wireGalaxyMenu()
                    wireFps()
                    wireSignInDialog()
                    addGlobalFunctions(appGlobalFunctions())

                    // td: reimplement as sidebar option
                    // wireRightPanel()
                    wireToaster()
                    wireOmni(omni)
                    wireRouteDock()

                    // hides the element that holds server rendered content
                    // document.getElementById(KoalaBody.ShellMount).modify(DisplayNone)
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

