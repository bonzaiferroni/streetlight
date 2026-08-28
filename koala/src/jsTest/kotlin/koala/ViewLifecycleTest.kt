@file:OptIn(ExperimentalCoroutinesApi::class)

package koala

import koala.dom.AppContainer
import koala.dom.View
import koala.dom.mountChildView
import koala.dom.mountRootView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.html.id
import kotlinx.html.js.div
import org.koin.dsl.koinApplication
import web.dom.document
import web.html.HTMLElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class ViewLifecycleTest {

    private fun testApp() = AppContainer(koinApplication { }.koin)

    private fun TestScope.mountTestRoot(): View {
        val host = document.createElement("div") as HTMLElement
        return host.mountRootView("test-root", backgroundScope, testApp()) { }
    }

    @Test
    fun disposalRunsDeepestFirstAndReversed() = runTest {
        val root = mountTestRoot()
        val log = mutableListOf<String>()

        val childA = root.mountChildView("childA", document.createElement("div")) {
            onDispose { log.add("childA") }
        }
        childA.mountChildView("grand", document.createElement("div")) {
            onDispose { log.add("grand") }
        }
        root.mountChildView("childB", document.createElement("div")) {
            onDispose { log.add("childB") }
        }

        root.dispose()

        assertEquals(listOf("childB", "grand", "childA"), log)
    }

    @Test
    fun disposedViewRefusesNewWork() = runTest {
        val root = mountTestRoot()
        root.dispose()

        assertFailsWith<IllegalStateException> { root.onDispose { } }
        assertFailsWith<IllegalStateException> {
            root.mountChildView("late", document.createElement("div")) { }
        }
    }

    @Test
    fun disposeCancelsRunningEffects() = runTest {
        val root = mountTestRoot()
        var ticks = 0
        root.launchEffect("ticker") {
            while (true) {
                delay(100.milliseconds)
                ticks++
            }
        }

        advanceTimeBy(350.milliseconds)
        assertEquals(3, ticks)

        root.dispose()
        advanceTimeBy(1_000.milliseconds)
        assertEquals(3, ticks)
    }

    @Test
    fun failedBuildRollsBackAndDisposes() = runTest {
        val root = mountTestRoot()
        val mount = document.createElement("div") as HTMLElement
        var disposerRan = false

        assertFailsWith<IllegalStateException> {
            root.mountChildView("doomed", mount) {
                onDispose { disposerRan = true }
                div { id = "doomed-cargo" }
                error("shipwreck")
            }
        }

        assertTrue(disposerRan)
        assertNull(mount.querySelector("#doomed-cargo"))
        assertTrue(mount.textContent!!.contains("Something went wrong."))
    }
}