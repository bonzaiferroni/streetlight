package koala.dom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

internal class RenderJob(
    val job: Job,
    val scope: CoroutineScope,
    val elements: List<HTMLElement>
) {
    val firstElement get() = elements.first()
}

internal fun <T> DOMRender.createRenderJob(
    parent: HTMLElement,
    value: T,
    block: DOMRender.(T) -> Unit
): RenderJob {
    parent.clear()
    val job = SupervisorJob()
    val scope = CoroutineScope(renderScope.coroutineContext + job)
    val elements = parent.append {
        val context = DOMRenderContext(this@append, app, scope, parent)
        context.block(value)
    }
    return RenderJob(job, scope, elements)
}

internal fun DOMRender.createRenderJob(
    parent: HTMLElement,
    block: DOMRender.() -> Unit
) = createRenderJob(parent, Unit) {
    block()
}
