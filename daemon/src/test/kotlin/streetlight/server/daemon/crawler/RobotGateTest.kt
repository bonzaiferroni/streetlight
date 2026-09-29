package streetlight.server.daemon.crawler

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class RobotGateTest {

    @Test
    fun `callers arriving together are spaced by the gate's interval`() = runBlocking {
        val gate = RobotGate(null, defaultDelay = 200.milliseconds)
        val start = TimeSource.Monotonic.markNow()

        val opened = List(3) { async { gate.waitUntilOpen("/events"); start.elapsedNow() } }.awaitAll().sorted()

        assertTrue(opened[1] - opened[0] >= 180.milliseconds, "second opened at ${opened[1]}")
        assertTrue(opened[2] - opened[1] >= 180.milliseconds, "third opened at ${opened[2]}")
    }
}
