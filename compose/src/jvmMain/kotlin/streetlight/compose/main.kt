package streetlight.compose

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.maplibre.compose.desktop.ProvideMapPresentationHost
import org.maplibre.compose.desktop.rememberAwtComposeMapPresentationHost

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Streetlight") {
        // MapLibre presents its native map through the window
        ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
            App()
        }
    }
}
