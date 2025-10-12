package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pondui.ui.controls.Button
import pondui.ui.controls.Column
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.modifiers.onEnterPressed
import pondui.ui.services.rememberMidiPlayer
import streetlight.app.RuntimeProvider

@Composable
fun PlayMidi() {
    val midi = rememberMidiPlayer()

    Button("Play Note") { midi.play(60) }
    Button("Play Chords") {
        midi.play(60)
        midi.play(64)
        midi.play(67)
    }
}

@Composable
fun HelloGemini() {
    val client = RuntimeProvider.gemini
    var responseText by remember { mutableStateOf("") }
    var requestText by remember { mutableStateOf("Hello Gemini") }

    Column(1) {
        Row(1) {
            TextField(requestText, onChange = { requestText = it }, modifier = Modifier.onEnterPressed {
                responseText = client.textChat(requestText) ?: "no response"
                requestText = ""
            })
        }
        Text(responseText)
    }
}