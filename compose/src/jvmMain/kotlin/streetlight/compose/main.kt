package streetlight.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import streetlight.compose.resources.Res
import streetlight.compose.resources.foxicon
import java.awt.Dimension
import streetlight.compose.model.rememberPortal
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
        // the window answers the desktop's back key and mouse button, so it holds the app's portal
        val portal = rememberPortal()
        Window(
            onCloseRequest = ::exitApplication,
            state = state,
            title = "Streetlight",
            icon = painterResource(Res.drawable.foxicon),
            undecorated = true,
            onPreviewKeyEvent = portal::backOn,
        ) {
            // the window keeps room for its title between the bar's buttons
            LaunchedEffect(window) { window.minimumSize = Dimension(WindowMinWidth, WindowMinHeight) }
            // MapLibre presents its native map through the window
            ProvideMapPresentationHost(rememberAwtComposeMapPresentationHost(window)) {
                Box(Modifier.fillMaxSize().backOnMouse(portal)) {
                    App(portal, bar = { TitleBar(state, onClose = ::exitApplication) })
                    ResizeEdges(state)
                }
            }
        }
    }
}
