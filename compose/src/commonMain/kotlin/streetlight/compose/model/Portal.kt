package streetlight.compose.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * The app's back stacks, one in [stacks] for each dock route and starting at it, and the moves along them. The stack
 * of [dockRoute] is shown above the stack of [start], as a tab bar keeps a stack per tab.
 */
class Portal(val start: AppRoute, val stacks: Map<AppRoute, NavBackStack<AppRoute>>, dockRouteState: MutableState<AppRoute>) {
    /** The dock route whose stack is shown. */
    var dockRoute by dockRouteState
        private set

    /** The dock routes whose stacks are shown, [start]'s beneath [dockRoute]'s. */
    val shownDockRoutes: List<AppRoute>
        get() = listOf(start, dockRoute).distinct()

    /** The routes shown, from the bottom of [start]'s stack to the top of [dockRoute]'s. */
    val backStack: List<AppRoute>
        get() = shownDockRoutes.flatMap { stacks.getValue(it) }

    private val stack get() = stacks.getValue(dockRoute)

    /** Opens [route] above the current one. */
    fun open(route: AppRoute) {
        stack.add(route)
    }

    /** Shows [route] in place of the current one. */
    fun swap(route: AppRoute) {
        stack[stack.lastIndex] = route
    }

    /** Shows the stack of the dock route [route], or returns to its first route when it is already shown. */
    fun dock(route: AppRoute) {
        when (route) {
            dockRoute -> stack.closeAboveFirst()
            else -> dockRoute = route
        }
    }

    /** Returns to [start], closing every route above it. */
    fun home() {
        dockRoute = start
        stack.closeAboveFirst()
    }

    /** Returns to the previous route, from the first of a dock route's stack to [start]'s, staying on [start]. */
    fun back() {
        when {
            stack.size > 1 -> stack.removeLast()
            dockRoute != start -> dockRoute = start
        }
    }
}

private fun NavBackStack<AppRoute>.closeAboveFirst() {
    while (size > 1) removeLast()
}

/** A [Portal] with a stack for each route of [dock], starting at the first, whose stacks survive as saved state. */
@Composable
fun rememberPortal(dock: List<DockRoute> = StableDock): Portal {
    val start = dock.first().route
    val stacks = dock.associate { dockRoute ->
        dockRoute.route to key(dockRoute.route) {
            rememberSerializable(serializer = NavBackStackSerializer(AppRoute.serializer())) {
                NavBackStack(dockRoute.route)
            }
        }
    }
    val dockRouteState = rememberSerializable(serializer = MutableStateSerializer(AppRoute.serializer())) {
        mutableStateOf(start)
    }
    return remember(stacks, dockRouteState) { Portal(start, stacks, dockRouteState) }
}

/** The app's [Portal], provided once at its root. */
val LocalPortal = staticCompositionLocalOf<Portal> { error("No Portal provided") }

/** The [Portal] [LocalPortal] provides. */
val currentPortal: Portal
    @Composable @ReadOnlyComposable get() = LocalPortal.current
