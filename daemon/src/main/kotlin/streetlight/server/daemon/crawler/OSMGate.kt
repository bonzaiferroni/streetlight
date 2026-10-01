package streetlight.server.daemon.crawler

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** The pacing of the crawler's searches of OpenStreetMap, one every [interval]. */
class OSMGate(val interval: Duration = 2.seconds) {
    private val slotMutex = Mutex()
    private var nextOpen = Instant.DISTANT_PAST

    /**
     * Waits for the caller's turn to search: each caller reserves the next slot, one [interval] after the last, so
     * searches stay spaced even when callers arrive together.
     */
    suspend fun waitUntilOpen() {
        val slot = slotMutex.withLock {
            maxOf(Clock.System.now(), nextOpen).also { nextOpen = it + interval }
        }
        delay(slot - Clock.System.now())
    }
}
