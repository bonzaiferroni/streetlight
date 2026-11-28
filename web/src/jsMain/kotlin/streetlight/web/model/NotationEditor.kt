package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.handledTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import streetlight.model.music.NotationId
import streetlight.model.music.Study
import streetlight.model.music.nameOf
import streetlight.model.music.notationOf
import streetlight.model.music.text.NotationError
import streetlight.model.music.text.NotationStyle
import streetlight.model.music.text.notationParseOf
import streetlight.model.music.trackOf
import streetlight.model.music.ui.musicSourceOf
import streetlight.model.music.withNotation

class NotationEditor(
    scope: CoroutineScope,
    notationStyleState: Tap<NotationStyle>,
    private val notationId: NotationId,
    private val host: MusicHost,
) {
    private val initialTrack = host.readTrack(notationId)
    private val trackId get() = initialTrack.trackId
    private val state = storeOf(NotationEditorState())
    val sheetState = host.sheetState
    val notationState = sheetState.tapOf { it.notationOf(notationId) }
    val musicSourceState = sheetState.tapOf { it.musicSourceOf(notationId) }
    val trackState = sheetState.tapOf { it.trackOf(notationId) }
    val nameState = trackState.tapOf { it.nameOf(notationId) }
    val errorsState = state.tapOf { it.errors }
    val studyState = host.studiesState.tapOf { studies -> studies.firstOrNull { it.notationId == notationId } }
    val isActiveStudy = studyState.handledTapOf({ it?.isActive ?: false }) { setIsActiveStudy(it) }

    val notationTextEditor = NotationTextEditor(scope, notationId, host, notationStyleState)

    init {
        notationTextEditor.textState.reactIn(scope, 1) { text ->
            val notation = notationState.now
            val parse = sheetState.now.notationParseOf(notationId, initialTrack.trackType, notation.name, text)

            state.set { copy(errors = parse.errors) }
            val track = host.readTrack(trackId).withNotation(parse.value)
            host.update(track)
        }
    }

    private fun setIsActiveStudy(isActive: Boolean) {
        val study = studyState.now ?: Study(trackId, notationId, isActive)
        host.upsert(study.copy(isActive = isActive))
    }
}

data class NotationEditorState(
    val errors: List<NotationError> = emptyList()
)