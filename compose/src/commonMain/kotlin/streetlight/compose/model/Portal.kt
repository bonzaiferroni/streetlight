package streetlight.compose.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.serialization.NavBackStackSerializer

/** The app's back stack, and the moves along it. */
class Portal(val backStack: NavBackStack<AppRoute>) {
    /** Opens [route] above the current one. */
    fun open(route: AppRoute) {
        backStack.add(route)
    }

    /** Returns to the previous route, staying on the first. */
    fun back() {
        if (backStack.size > 1) backStack.removeLast()
    }
}

/** A [Portal] starting at [start], whose back stack survives as saved state. */
@Composable
fun rememberPortal(start: AppRoute): Portal {
    val backStack = rememberSerializable(serializer = NavBackStackSerializer(AppRoute.serializer())) {
        NavBackStack(start)
    }
    return Portal(backStack)
}
