package streetlight.app

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import pondui.CacheFile
import pondui.WatchWindow
import pondui.WindowSize
import pondui.ui.controls.Text
import pondui.ui.nav.NavRoute

fun main() {
    application {
        val cacheFlow = CacheFile("appcache.json") { AppCache() }
        val cache by cacheFlow.collectAsState()

        val windowState = WatchWindow(cache.windowSize) {
            cacheFlow.value = cacheFlow.value.copy(windowSize = it)
        }

        Window(
            state = windowState,
            onCloseRequest = ::exitApplication,
            title = "App",
            undecorated = true,
        ) {
            App(
                changeRoute = { cacheFlow.value = cache.copy(address = it.toPath()) },
                exitApp = ::exitApplication
            )
        }
    }
}

@Serializable
data class AppCache(
    val windowSize: WindowSize = WindowSize(600, 800),
    val address: String? = null
)