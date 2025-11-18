package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import pondui.ui.controls.Text
import streetlight.model.data.DrumSequence
import streetlight.model.data.VocalSequence

@Composable
fun DrumSequenceView(
    sequence: DrumSequence,
) {
    Text("Todo: DrumSequenceView", modifyStyle = { it.copy(textAlign = TextAlign.Center)})
}