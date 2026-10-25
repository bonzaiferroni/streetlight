package streetlight.compose

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import org.maplibre.compose.desktop.rememberAwtComposeMapPresentationHost

fun main() {
    // read before the window opens, so it opens at its saved frame
    val frame = runBlocking { readWindowFrame() }
    application {
        val state = rememberWindowState(frame)
        LaunchedEffect(state) { state.saveFrames(frame) }
        Window(onCloseRequest = ::exitApplication, state = state, title = "Streetlight") {
            // MapLibre presents its native map through the window
            ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
                App()
            }
        }
    }
}
