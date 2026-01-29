package streetlight.web

import kotlinx.coroutines.CoroutineScope

class AppPortal(
    initialPath: String,
    scope: CoroutineScope
): BrowserModel<AppNavigatorState>(AppNavigatorState(initialPath), scope) {
    val screenFlow = stateFlow.mapDistinct { it.screen }

    fun go(screen: AppScreen) {
        setState { it.copy(screen = screen) }
    }
}

data class AppNavigatorState(
    val path: String,
    val screen: AppScreen = AppScreen.Home
)

enum class AppScreen {
    Home,
    Event,
    User
}