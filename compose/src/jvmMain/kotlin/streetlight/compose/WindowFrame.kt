package streetlight.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.runningFold
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import streetlight.compose.io.readAppFile
import streetlight.compose.io.writeAppFile

/** The window's size and position when floating, in dp, and whether it is maximized. */
@Serializable
data class WindowFrame(
    val width: Int = 800,
    val height: Int = 600,
    val x: Int? = null,
    val y: Int? = null,
    val isMaximized: Boolean = false,
)

/** Reads the saved [WindowFrame], or the default when there is none or it cannot be read. */
suspend fun readWindowFrame(): WindowFrame =
    readAppFile(WINDOW_FILE)?.let { runCatching { Json.decodeFromString<WindowFrame>(it) }.getOrNull() } ?: WindowFrame()

/** A [WindowState] opening at [frame]. */
@Composable
fun rememberWindowState(frame: WindowFrame) = rememberWindowState(
    placement = if (frame.isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating,
    position = if (frame.x != null && frame.y != null) WindowPosition(frame.x.dp, frame.y.dp) else WindowPosition.PlatformDefault,
    size = DpSize(frame.width.dp, frame.height.dp),
)

/**
 * Saves this window's frame, starting from [frame], once each change settles. The size and position are kept only while
 * the window floats, so a maximized window reopens to its floating frame when restored.
 */
@OptIn(FlowPreview::class)
suspend fun WindowState.saveFrames(frame: WindowFrame) {
    snapshotFlow { Triple(placement, size, position) }
        .runningFold(frame) { saved, (placement, size, position) ->
            when (placement) {
                WindowPlacement.Floating -> saved.copy(
                    width = size.width.value.toInt(),
                    height = size.height.value.toInt(),
                    x = position.x.takeIf { it.isSpecified }?.value?.toInt(),
                    y = position.y.takeIf { it.isSpecified }?.value?.toInt(),
                    isMaximized = false,
                )
                WindowPlacement.Maximized -> saved.copy(isMaximized = true)
                WindowPlacement.Fullscreen -> saved
            }
        }
        .drop(1)
        .distinctUntilChanged()
        .debounce(SAVE_DELAY_MILLIS)
        .collect { writeAppFile(WINDOW_FILE, Json.encodeToString(it)) }
}

private const val WINDOW_FILE = "window.json"

/** The time a frame holds before it is saved, so a drag writes once. */
private const val SAVE_DELAY_MILLIS = 500L
