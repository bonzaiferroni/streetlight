package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kabinet.utils.AudioStage
import kabinet.utils.PcmLayer
import kotlinx.coroutines.launch
import pondui.ui.controls.Button
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.services.WavePlayer
import pondui.ui.services.rememberMicRecorder
import pondui.ui.services.toPcmByteArray

@Composable
fun HelloScreen(
) {
    val player = remember { WavePlayer() }
    val scope = rememberCoroutineScope()
    val stage = remember { AudioStage() }
    val mic = rememberMicRecorder {
        stage.add(PcmLayer(it))
    }
    val micState by mic.stateFlow.collectAsState()
    Scaffold {
        val isRecording = micState.isRecording
        Button(if (isRecording) "Stop" else "Record") {
            if (isRecording) {
                mic.stop()
                scope.launch {
                    player.getStream().use { stream ->
                        stage.play { pcm ->
                            stream.write(pcm.toPcmByteArray())
                        }
                    }
                }
            } else {
                mic.start()
            }
        }
    }
}

// Data flow:
// MicRecorder -> PcmLayer -> AudioStage -> WavePlayer (pcm in chunks)

fun ContentScope.contentBox(
    content: @Composable () -> Unit
) {
    addContent(content)
}

class ContentScope {
    var content: (@Composable () -> Unit)? = null

    fun addContent(content: @Composable () -> Unit) {
        this.content = content
    }
}