package streetlight.web.model

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import streetlight.model.music.text.NotationError

/** The text a user writes in a notation, starting from [initialText], and the problems found reading it. */
class NotationField(
    initialText: String? = null,
) {
    private val state = storeOf(NotationFieldState(initialText))
    val textState = state.mutableTapOf({ it.text.orEmpty() }) { copy(text = it) }
    val errorsState = state.tapOf { it.errors }

    fun setText(text: String?) = state.update { it.copy(text = text) }

    /** Reads the text with [read] whenever it changes, keeping the problems [read] returns. */
    fun launchReads(scope: CoroutineScope, read: (String) -> List<NotationError>) {
        scope.launch {
            state.flow.map { it.text }.filterNotNull().distinctUntilChanged().collect { text ->
                val errors = read(text)
                state.update { it.copy(errors = errors) }
            }
        }
    }

    /**
     * Rewrites the text with [write] whenever [changes] emits after its first value, as when the key the text is
     * written against changes.
     */
    fun launchRewrites(scope: CoroutineScope, changes: Flow<Any?>, write: () -> String?) {
        scope.launch {
            changes.drop(1).collect { setText(write()) }
        }
    }
}

data class NotationFieldState(
    val text: String? = null,
    val errors: List<NotationError> = emptyList(),
)
