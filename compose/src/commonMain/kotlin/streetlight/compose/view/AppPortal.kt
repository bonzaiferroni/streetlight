package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import streetlight.compose.model.AppRoute
import streetlight.compose.model.EarthRoute
import streetlight.compose.model.EventRoute
import streetlight.compose.model.GalaxyRoute
import streetlight.compose.model.HomeRoute
import streetlight.compose.model.LocationRoute
import streetlight.compose.model.currentPortal
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Renders the route on top of the shown back stacks, each entry with its own saved state and view models, kept while
 * its stack is hidden. A change of page fades the old page out, then the new one in, as the web's portal does; the new
 * page's content is read from the start of the fade-out, so it is often ready by the time it fades in. The parts the
 * two pages share move between them across the [LocalSharedTransitionScope].
 */
@Composable
fun AppPortal() {
    val portal = currentPortal
    val magic = currentTheme.magic
    val fadeOut = fadeOut(tween(magic.interval, easing = magic.easing))
    val fadeIn = fadeIn(tween(magic.interval, delayMillis = magic.interval, easing = magic.easing))

    val provider = entryProvider<AppRoute> {
        entry<HomeRoute> { HomeScreen() }
        entry<GalaxyRoute> { GalaxyScreen(it) }
        entry<EventRoute> { EventScreen(it) }
        entry<LocationRoute> { LocationScreen(it) }
        entry<EarthRoute> { EarthScreen() }
    }
    // each stack decorates its own entries, so a hidden stack's pages keep their state and view models
    val stackEntries = portal.stacks.mapValues { (_, stack) ->
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = provider,
        )
    }
    val entries = portal.shownDockRoutes.flatMap { stackEntries.getValue(it) }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavDisplay(
                entries = entries,
                onBack = portal::back,
                sharedTransitionScope = this,
                transitionSpec = { fadeIn togetherWith fadeOut },
                popTransitionSpec = { fadeIn togetherWith fadeOut },
                predictivePopTransitionSpec = { fadeIn togetherWith fadeOut },
            )
        }
    }
}
