package koala.dom

import koala.core.ViewTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import org.w3c.dom.HTMLElement

class View(
    consumer: TagScope,
    parentScope: CoroutineScope,
    override val name: String,
    override val app: AppContainer,
    override val mount: HTMLElement,
    override val parent: ViewScope?
): ViewScope, TagScope by consumer {

    private val disposers: MutableList<() -> Unit> = mutableListOf()
    private val children: MutableList<View> = mutableListOf()
    private val parentView get() = parent as? View // ensured by sealed interface
    private var disposed = false

    override val scope = CoroutineScope(
        parentScope.coroutineContext
                + SupervisorJob(parentScope.coroutineContext[Job])
                + ViewTelemetry(this)
    )

    init {
        parentView?.addChild(this)
    }

    private fun addChild(view: View) {
        if (disposed) error("added child to disposed view: $name")
        children.add(view)
    }

    private fun removeChild(view: View) {
        children.remove(view)
    }

    override fun onDispose(block: () -> Unit) {
        if (disposed) error("onDispose registered on a disposed view: $name")
        disposers += block
    }

    internal fun dispose() {
        if (disposed) return
        disposed = true
        children.toList().asReversed().forEach { it.dispose() }
        disposers.asReversed().forEach { it() }
        scope.coroutineContext.job.cancel()
        parentView?.removeChild(this)
    }
}