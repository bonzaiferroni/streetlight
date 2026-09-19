package streetlight.web.integration

import koala.dom.View
import koala.dom.asW3C
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.test.fail
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

fun View.editor(label: String): HTMLElement =
    mount.asW3C().querySelector("[data-block-label='$label'][role='textbox']") as? HTMLElement
        ?: fail("no editor labelled '$label'")

fun View.button(label: String): HTMLButtonElement {
    val buttons = mount.asW3C().querySelectorAll("button")
    for (index in 0 until buttons.length) {
        val button = buttons.item(index) as? HTMLButtonElement ?: continue
        if (button.textContent?.trim() == label) return button
    }
    fail("no button labelled '$label'")
}

suspend fun View.writeIn(label: String, text: String) {
    val element = editor(label)
    val block = element.children.item(0) ?: document.createElement("div").also { element.appendChild(it) }
    block.textContent = text
    element.dispatchEvent(Event("input"))
    awaitUntil("the editor '$label' to hold \"$text\"") {
        editor(label).textContent?.contains(text) == true
    }
}

fun View.clickButton(label: String) = button(label).click()

suspend fun View.awaitText(text: String) =
    awaitUntil("the view to show \"$text\"") { mount.asW3C().textContent?.contains(text) == true }

suspend fun View.awaitEditorCleared(label: String) =
    awaitUntil("the editor '$label' to clear") { editor(label).textContent.isNullOrBlank() }

suspend fun awaitUntil(
    description: String,
    timeout: Duration = AwaitTimeout,
    predicate: () -> Boolean,
) {
    val mark = TimeSource.Monotonic.markNow()
    while (!predicate()) {
        if (mark.elapsedNow() > timeout) fail("timed out after $timeout awaiting $description")
        delay(AwaitInterval)
    }
}

private val AwaitTimeout = 2.seconds
private val AwaitInterval = 10.milliseconds
