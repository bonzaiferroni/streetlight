package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import streetlight.compose.model.EventRoute
import streetlight.compose.model.GalaxyRoute
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.currentPortal

/** Renders the route on top of the back stack, each entry with its own saved state and view models. */
@Composable
fun AppPortal() {
    val portal = currentPortal
    NavDisplay(
        backStack = portal.backStack,
        onBack = portal::back,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<HomeRoute> { HomeScreen() }
            entry<GalaxyRoute> { GalaxyScreen(it) }
            entry<EventRoute> { EventScreen(it) }
        },
    )
}
