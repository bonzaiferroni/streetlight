package streetlight.app.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pondui.ui.controls.TextField
import pondui.ui.theme.Pond
import streetlight.model.data.PartSequence
import streetlight.model.data.VocalSequence

@Composable
fun VocalSequenceEditor(
    sequence: VocalSequence,
    modifySequence: (PartSequence?) -> Unit
) {
    TextField(
        text = sequence.lyrics,
        style = Pond.typo.mono,
        modifier = Modifier.fillMaxWidth()
    ) { modifySequence(sequence.copy(lyrics = it)) }
}