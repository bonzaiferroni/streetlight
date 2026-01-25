package streetlight.web

import kotlinx.coroutines.CoroutineScope

class AppNavigator(
    initialPath: String,
    scope: CoroutineScope
): BrowserModel<AppNavigatorState>(AppNavigatorState(initialPath), scope) {
}

data class AppNavigatorState(
    val path: String,
    val screen: BrowserScreen = BrowserScreen.Home
)

enum class BrowserScreen {
    Home,
    Event,
    User
}