package koala.dom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.dom.clear
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

// internal class RenderJob(
//     val job: Job,
//     val scope: CoroutineScope,
//     val elements: List<HTMLElement>,
//     private val disposers: List<suspend () -> Unit>
// ) {
//     val firstElement get() = elements.first()
//
//     internal suspend fun dispose() {
//         disposers.asReversed().forEach { it() }
//         job.cancel()
//     }
// }
//
// internal fun <T> EffectScope.createRenderJob(
//     name: String,
//     parent: HTMLElement,
//     value: T,
//     block: FlowView.(T) -> Unit
// ): RenderJob {
//     parent.clear()
//     val job = SupervisorJob(parentScope.coroutineContext[Job])
//     val telemetry = ScopeTelemetry(name, parent, parentScope.coroutineContext[ScopeTelemetry])
//     val scope = CoroutineScope(parentScope.coroutineContext + job + telemetry)
//     val disposers = mutableListOf<suspend () -> Unit>()
//     val elements =
//     return RenderJob(job, scope, elements, disposers)
// }
//
// internal fun EffectScope.createRenderJob(
//     name: String,
//     parent: HTMLElement,
//     block: ViewScope.() -> Unit
// ) = createRenderJob(name, parent, Unit) {
//     block()
// }
