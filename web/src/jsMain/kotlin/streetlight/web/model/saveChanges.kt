@file:OptIn(FlowPreview::class)

package streetlight.web.model

import kampfire.model.Tap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.withContext

/**
 * Saves each value of this tap with [save] once changes pause for [delayMillis], and the last one unsaved as the
 * collecting scope ends.
 */
internal suspend fun <T> Tap<T>.saveChanges(delayMillis: Long = SAVE_DELAY_MILLIS, save: suspend (T) -> Unit) {
    var saved = now
    try {
        flow.debounce(delayMillis).collect { value ->
            if (value == saved) return@collect
            save(value)
            saved = value
        }
    } finally {
        val value = now
        if (value != saved) withContext(NonCancellable) { save(value) }
    }
}

private const val SAVE_DELAY_MILLIS = 1_000L
