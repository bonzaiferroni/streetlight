package streetlight.web.subApp

import koala.dom.AppContainer
import koala.dom.mountRootView
import koala.interop.KtFunction
import koala.interop.addGlobalFunctions
import koala.model.Portal
import koala.modifier.KoalaBody
import koala.utility.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.koinApplication
import streetlight.web.interop.clearFeedTag
import streetlight.web.interop.morePosts
import streetlight.web.interop.readEntityRef
import streetlight.web.interop.searchFeed
import streetlight.web.interop.sortByMark
import streetlight.web.interop.wireEntityDialog
import streetlight.web.interop.wireTagFilterMenu
import streetlight.web.interop.wireToaster
import streetlight.web.component.TagFilterMenu
import streetlight.web.layout.EntityDialog
import streetlight.web.layout.FeedSection

/** Starts a LocationApp: the services of [locationAppModule], its portal, and the parts of the page it wires. */
fun viewLocationApp() {
    val koin = koinApplication {
        modules(locationAppModule)
    }.koin

    val app = AppContainer(koin)

    with (app) {
        val scope: CoroutineScope = get()
        val portal: Portal = get()

        scope.launch(::viewLocationApp) {
            try {
                mountRootView(KoalaBody.PortalMount, scope, app) {
                    viewLocationPortal()
                    wireEntityDialog()
                    wireTagFilterMenu()
                    wireToaster()
                    addGlobalFunctions(listOf(
                        KtFunction(FeedSection.SortByMark, ::sortByMark),
                        KtFunction(FeedSection.MorePosts, ::morePosts),
                        KtFunction(TagFilterMenu.ClearTag, ::clearFeedTag),
                        KtFunction(FeedSection.SearchFeed, ::searchFeed),
                        KtFunction(EntityDialog.Open, ::readEntityRef),
                    ))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                portal.notifyWrecked()
                throw e
            }
        }
    }
}
