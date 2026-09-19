@file:OptIn(ExperimentalCoroutinesApi::class)

package koala

import koala.utils.DropWhileBusy
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DropWhileBusyTest {

    private fun TestScope.busyScope() = CoroutineScope(
        SupervisorJob() + StandardTestDispatcher(testScheduler) + CoroutineExceptionHandler { _, _ -> }
    )

    @Test
    fun `a launch requested while one is running is dropped`() = runTest {
        val scope = busyScope()
        val gate = DropWhileBusy()
        val release = CompletableDeferred<Unit>()
        var runs = 0

        val first = gate.launch(scope, "first") {
            runs++
            release.await()
        }
        val second = gate.launch(scope, "second") { runs++ }
        advanceUntilIdle()

        assertNotNull(first, "the first launch should start")
        assertNull(second, "a launch while one is running should be dropped")

        release.complete(Unit)
        advanceUntilIdle()

        assertEquals(1, runs, "only the first block should have run")
    }

    @Test
    fun `a launch is accepted after the previous one completes`() = runTest {
        val scope = busyScope()
        val gate = DropWhileBusy()
        var runs = 0

        gate.launch(scope, "first") { runs++ }
        advanceUntilIdle()
        val second = gate.launch(scope, "second") { runs++ }
        advanceUntilIdle()

        assertNotNull(second, "a launch after completion should start")
        assertEquals(2, runs)
    }

    @Test
    fun `a launch is accepted after the previous one fails`() = runTest {
        val scope = busyScope()
        val gate = DropWhileBusy()
        var runs = 0

        gate.launch(scope, "first") { error("the first launch fails") }
        advanceUntilIdle()
        val second = gate.launch(scope, "second") { runs++ }
        advanceUntilIdle()

        assertNotNull(second, "a launch after a failure should start")
        assertEquals(1, runs)
    }

    @Test
    fun `a launch is accepted after the previous one is cancelled`() = runTest {
        val scope = busyScope()
        val gate = DropWhileBusy()
        var runs = 0

        val first = assertNotNull(gate.launch(scope, "first") { awaitCancellation() })
        advanceUntilIdle()
        first.cancel()
        advanceUntilIdle()
        val second = gate.launch(scope, "second") { runs++ }
        advanceUntilIdle()

        assertNotNull(second, "a launch after a cancellation should start")
        assertEquals(1, runs)
    }
}
