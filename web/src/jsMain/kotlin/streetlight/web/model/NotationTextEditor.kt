package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import streetlight.model.music.Key
import streetlight.model.music.NotationId
import streetlight.model.music.TimeSignature
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.notationTextOf

class NotationTextEditor(
    scope: CoroutineScope,
    private val notationId: NotationId,
    private val host: MusicHost,
    private val notationStyleState: Tap<NotationStyle>,
) {
    private val sheetState = host.sheetState
    private val state = sheetState.now.let { sheet ->
        val text = notationTextOf(host.readNotation(notationId), sheet.music.key, sheet.music.time)
        storeOf(NotationTextEditorState(text))
    }
    val textState = state.mutableTapOf({ it.text }) { copy(text = it) }

    init {
        val changes = combine(sheetState.flow, notationStyleState.flow) { sheet, style ->
            TextBasis(sheet.music.key, sheet.music.time, style)
        }

        scope.launch {
            changes.drop(1).collect { basis ->
                val text = notationTextOf(host.readNotation(notationId), basis.key, basis.time)
                state.set { copy(text = text) }
            }
        }
    }
}

data class NotationTextEditorState(
    val text: String = "",
)

private data class TextBasis(
    val key: Key,
    val time: TimeSignature,
    val style: NotationStyle,
)