package streetlight.compose.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import kotlin.time.Clock

/** Prints each frame that takes longer than [thresholdMillis] while the caller is composed, for chasing jank. */
@Composable
fun LongFrameLog(thresholdMillis: Long = 32) {
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        while (true) withFrameNanos { now ->
            val millis = (now - last) / 1_000_000
            if (millis > thresholdMillis) println("[jank] long frame ${millis}ms at ${Clock.System.now()}")
            last = now
        }
    }
}

/** Prints when the caller enters and leaves composition, under [tag], to line up against a [LongFrameLog]. */
@Composable
fun CompositionLog(tag: String) {
    DisposableEffect(Unit) {
        println("[jank] $tag composed at ${Clock.System.now()}")
        onDispose { println("[jank] $tag disposed at ${Clock.System.now()}") }
    }
}
