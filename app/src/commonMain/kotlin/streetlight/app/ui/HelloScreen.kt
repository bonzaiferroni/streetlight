package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import pondui.ui.controls.Button
import pondui.ui.controls.Scaffold
import pondui.ui.services.createMidiPlayer
import pondui.ui.services.rememberMidiPlayer

@Composable
fun HelloScreen(
) {
    val midi = rememberMidiPlayer()
    Scaffold {
        Button("Play Note") { midi.play(60) }
        Button("Play Chords") {
            midi.play(60)
            midi.play(64)
            midi.play(67)
        }
    }
}

