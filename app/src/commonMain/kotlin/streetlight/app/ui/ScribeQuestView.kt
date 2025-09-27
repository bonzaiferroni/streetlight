package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pondui.ui.controls.Column
import pondui.ui.controls.Text
import pondui.ui.services.SttConfig
import pondui.ui.services.SttEvent
import pondui.ui.services.rememberSpeechToText

@Composable
fun ScribeQuestView() {
    val stt = rememberSpeechToText()
    var text by remember { mutableStateOf("") }
    var partial by remember { mutableStateOf("")}
    LaunchedEffect(Unit) {
        stt.start()
        stt.events.collect {
            when (it) {
                SttEvent.EndOfUtterance -> text += "[EOU]"
                is SttEvent.Error -> text += "[ERROR]"
                is SttEvent.Final -> text += it.text
                is SttEvent.Partial -> partial = it.text
                SttEvent.Ready -> text += "[READY]"
            }
        }
    }

    Column(1) {
        Text(text)
        Text(partial)
    }
}