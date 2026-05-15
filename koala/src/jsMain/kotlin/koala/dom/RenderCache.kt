package koala.dom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

class RenderCache(
    val job: Job,
    val localScope: CoroutineScope,
    val elements: List<HTMLElement>
) {
    val firstElement get() = elements.first()
}

fun <T> RenderContext.createRender(
    parent: HTMLElement,
    value: T,
    block: RenderContext.(T) -> Unit
): RenderCache {
    val elements = replaceRender(parent) {
        block(value)
    }
    val job = parent.queryJob() ?: error("job not found")
    val scope = parent.queryScope() ?: error("scope not found")
    return RenderCache(job, scope, elements)
}

fun RenderContext.createRender(
    parent: HTMLElement,
    block: RenderContext.() -> Unit
) = createRender(parent, Unit) {
    block()
}