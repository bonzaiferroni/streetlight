package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kabinet.GEMINI_KEY
import kotlinx.coroutines.launch
import pondui.ui.controls.Button
import pondui.ui.controls.Scaffold
import pondui.ui.services.AudioSpec
import pondui.ui.services.PcmStream
import pondui.ui.services.rememberMicRecorder

@Composable
fun HelloScreen(
) {
    val scope = rememberCoroutineScope()
    val stream = remember { PcmStream(24000).also { it.start() } }
    val live = remember { GeminiLiveAudio(apiKey = GEMINI_KEY).also { it.connect() } }
    live.onModelAudio {
        println("received audio")
        scope.launch {
            stream.send(it)
        }
    }
    val recorder = rememberMicRecorder(AudioSpec(sampleRate = 16000)) { pcm ->
        println("sending audio from mic")
        live.sendPcmChunk(pcm, true)
    }

    val recorderState by recorder.stateFlow.collectAsState()

    Scaffold {
        if (recorderState.isRecording) {
            Button("Stop", onClick = recorder::stop)
        } else {
            Button("Start", onClick = recorder::start)
        }
    }
}
