package streetlight.app

import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.compose.rememberNavController
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import org.w3c.dom.Window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        var currentPath = window.getPath()
        val initialRoute = appConfig.toRoute(currentPath) ?: StartRoute

        val routeState = remember { MutableStateFlow(initialRoute) }

        window.addEventListener("hashchange") {
            val path = window.getPath()
            if (path == currentPath) return@addEventListener
            currentPath = path
            appConfig.toRoute(path)?.let {
                routeState.value = it
            }
        }

        App(
            routeState = routeState,
            changeRoute = { navRoute ->
                navRoute.toPath()?.let {
                    currentPath = it
                    window.location.hash = "/$it"
                }
            },
            exitApp = null,
        ) // go
    }
}

private fun Window.getPath() = this.location.hash.substringAfter("#/", "")