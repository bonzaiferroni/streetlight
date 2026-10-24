package streetlight.compose.view

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import streetlight.compose.model.EventFeed
import streetlight.compose.model.EventFeedRoute
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.Portal

/** Renders the route on top of the back stack, each entry with its own saved state and view models. */
context(portal: Portal)
@Composable
fun AppPortal() {
    NavDisplay(
        backStack = portal.backStack,
        onBack = portal::back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> { HomeView() }
            entry<EventFeedRoute> {
                context(koinViewModel<EventFeed>()) { EventFeedView() }
            }
        },
    )
}
