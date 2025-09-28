package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import pondui.ui.controls.Button
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
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        stt.events.collect {
            when (it) {
                // SttEvent.EndOfUtterance -> text += "[EOU]"
                // is SttEvent.Error -> text += "[ERROR]"
                is SttEvent.Final -> {
                    partial = ""
                    text += "${it.text}\n"
                }
                is SttEvent.Partial -> partial = it.text
                // SttEvent.Ready -> text += "[READY]"
                else -> { }
            }
        }
    }

    Column(1) {
        Text(text)
        Text(partial)
        Button("Listen") {
            scope.launch {
                stt.start()
            }
        }
    }
}