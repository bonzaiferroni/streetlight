package koala.dom

import koala.core.ScopeTelemetry
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

internal fun <T> EffectScope.createRenderJob(
    name: String,
    parent: HTMLElement,
    value: T,
    block: ViewScope.(T) -> Unit
): RenderJob {
    parent.clear()
    val job = SupervisorJob(parentScope.coroutineContext[Job])
    val telemetry = ScopeTelemetry(name, parent, parentScope.coroutineContext[ScopeTelemetry])
    val scope = CoroutineScope(parentScope.coroutineContext + job + telemetry)
    val elements = try {
        parent.append {
            val context = View(this@append, app, scope, parent)
            context.block(value)
        }
    } catch (e: Throwable) {
        parent.clear()
        parent.append {
            textBlock("Something went wrong.")
        }
        job.cancel()   // never leak a scope we can't hand over
        throw e
    }
    return RenderJob(job, scope, elements)
}

internal fun EffectScope.createRenderJob(
    name: String,
    parent: HTMLElement,
    block: ViewScope.() -> Unit
) = createRenderJob(name, parent, Unit) {
    block()
}
