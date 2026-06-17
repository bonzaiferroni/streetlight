package koala.dom

import koala.html.Id
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.dom.clear
import kotlinx.html.dom.append
import kotlinx.html.dom.prepend
import org.w3c.dom.HTMLElement
import kotlin.coroutines.cancellation.CancellationException

@RenderMarker
interface AppScope: TagScope, AppFacade {
    override val app: AppContainer
    val parent: HTMLElement
    val parentScope: CoroutineScope

    fun launchEffect(block: suspend EffectScope.() -> Unit) {
        val effectScope = EffectScope(app, parent, parentScope)
        parentScope.launch {
            try {
                effectScope.block()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                else console.error("Failed in launch, see object", e)
            }
        }
    }
}

interface AppFacade {
    val app: AppContainer
}

@DslMarker
annotation class RenderMarker

@RenderMarker
class EffectScope(
    override val app: AppContainer,
    val parent: HTMLElement,
    val parentScope: CoroutineScope,
): AppFacade {
    fun launch(block: suspend CoroutineScope.() -> Unit): Job =
        parentScope.launch {
            try {
                block()
            } catch (e: UnsupportedOperationException) {
                throw InvalidRenderOperation(parent)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                else console.error("Failed in launch, see object", e)
            }
        }
}

internal class RenderScope(
    consumer: TagScope,
    override val app: AppContainer,
    override val parentScope: CoroutineScope,
    override val parent: HTMLElement,
): AppScope, TagScope by consumer

fun HTMLElement.renderRoot(
    scope: CoroutineScope,
    app: AppContainer,
    block: AppScope.() -> Unit
): List<HTMLElement> {
    clear()
    return append {
        val context = RenderScope(this@append, app, provisionScope(scope, true), this@renderRoot)
        context.block()
    }
}

fun HTMLElement.replaceRender(
    app: AppContainer,
    parentScope: CoroutineScope,
    block: AppScope.() -> Unit,
): List<HTMLElement> {
    clear()
    return append {
        val context = RenderScope(this@append, app, provisionScope(parentScope, true), this@replaceRender)
        context.block()
    }
}

fun AppScope.replaceRender(
    element: HTMLElement,
    block: AppScope.() -> Unit,
) = element.replaceRender(app, parentScope, block)

fun clearRender(element: HTMLElement) {
    element.clear()
    element.clearScope()
}

fun HTMLElement.appendRender(
    app: AppContainer,
    parentScope: CoroutineScope,
    block: AppScope.() -> Unit
) = append {
    val context = RenderScope(this@append, app, provisionScope(parentScope, false), this@appendRender)
    context.block()
}

fun AppScope.appendRender(
    element: HTMLElement,
    block: AppScope.() -> Unit
) = element.appendRender(app, parentScope, block)

fun AppScope.prependRender(
    element: HTMLElement,
    block: AppScope.() -> Unit
) = element.prepend {
    val context = RenderScope(this@prepend, app, element.provisionScope(parentScope, false), element)
    context.block()
}

fun AppScope.replaceRender(
    id: Id,
    ancestor: HTMLElement? = null,
    block: AppScope.() -> Unit
) = replaceRender(((ancestor ?: document.body!!).querySelector(id) ?: error("element not found: $this")), block)

class InvalidRenderOperation(parent: HTMLElement): Exception("Appended to finalized element: ${parent.printPath()}")