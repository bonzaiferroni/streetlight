package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import streetlight.app.audio.AudioStage
import streetlight.app.audio.PcmLayer
import kotlinx.coroutines.launch
import pondui.ui.controls.Button
import pondui.ui.controls.Scaffold
import pondui.ui.services.WavePlayer
import pondui.ui.services.rememberMicRecorder
import pondui.ui.services.toPcmByteArray
import streetlight.app.audio.DrumLoopLayer
import streetlight.app.audio.addSample
import streetlight.app.audio.loadDrumKit

@Composable
fun HelloScreen(
) {
    val player = remember { WavePlayer() }
    val scope = rememberCoroutineScope()
    val stage = remember { AudioStage() }
    val mic = rememberMicRecorder {
        stage.add(PcmLayer(it))
    }
    scope.launch {
        val drumKit = loadDrumKit("DDD1", 44_100)
        stage.add(DrumLoopLayer(drumKit, 120))
    }

    val micState by mic.stateFlow.collectAsState()
    Scaffold {
        val isRecording = micState.isRecording
        Button(if (isRecording) "Stop" else "Record") {
            if (isRecording) {
                mic.stop()
                scope.launch(Dispatchers.IO) {
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

@Composable
fun <T> rememberSuspend(block: suspend () -> T): T? {
    val state = remember { mutableStateOf<T?>(null) }
    LaunchedEffect(Unit) { state.value = block() }
    return state.value
}