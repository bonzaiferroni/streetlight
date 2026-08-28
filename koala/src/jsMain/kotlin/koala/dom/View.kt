package koala.dom

import koala.core.ViewTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.html.Entities
import kotlinx.html.Tag
import kotlinx.html.Unsafe
import kotlinx.html.org.w3c.dom.events.Event
import web.html.HTMLElement

class View(
    private var consumer: AppendScope,
    parentScope: CoroutineScope,
    override val scopeName: String,
    override val app: AppContainer,
    override val mount: HTMLElement,
    override val parent: ViewScope?
): ViewScope, AppendScope {

    private val disposers: MutableList<() -> Unit> = mutableListOf()
    private val children: MutableList<View> = mutableListOf()
    private val parentView get() = parent?.resolveView()
    private var disposed = false

    override val scope = CoroutineScope(
        parentScope.coroutineContext
                + SupervisorJob(parentScope.coroutineContext[Job])
                + ViewTelemetry(this)
    )

    override var contentScope = createContentScope()
        private set

    private fun createContentScope() = CoroutineScope(
        scope.coroutineContext
                + SupervisorJob(scope.coroutineContext[Job])
    )

    init {
        parentView?.addChild(this)
    }

    private fun addChild(view: View) {
        if (disposed) error("added child to disposed view: $scopeName")
        children.add(view)
    }

    private fun removeChild(view: View) {
        children.remove(view)
    }

    override fun onDispose(block: () -> Unit) {
        // note: disposers belong to the content generation — they run on every
        // clear(), not only at true disposal
        if (disposed) error("onDispose registered on a disposed view: $scopeName")
        disposers += block
    }

    internal fun dispose() {
        if (disposed) return
        disposed = true
        clear()
        scope.coroutineContext.job.cancel()
        parentView?.removeChild(this)
    }

    internal fun clear() {
        contentScope.coroutineContext.job.cancel()
        children.toList().asReversed().forEach { it.dispose() }
        disposers.asReversed().forEach { it() }
        contentScope = createContentScope()
    }

    internal fun setConsumer(value: AppendScope) {
        consumer = value
    }

    override fun onTagStart(tag: Tag) = consumer.onTagStart(tag)
    override fun onTagAttributeChange(tag: Tag, attribute: String, value: String?) =
        consumer.onTagAttributeChange(tag, attribute, value)
    override fun onTagEvent(tag: Tag, event: String, value: (Event) -> Unit) =
        consumer.onTagEvent(tag, event, value)
    override fun onTagEnd(tag: Tag) = consumer.onTagEnd(tag)
    override fun onTagContent(content: CharSequence) = consumer.onTagContent(content)
    override fun onTagContentEntity(entity: Entities) = consumer.onTagContentEntity(entity)
    override fun onTagContentUnsafe(block: Unsafe.() -> Unit) = consumer.onTagContentUnsafe(block)
    override fun onTagComment(content: CharSequence) = consumer.onTagComment(content)
    override fun finalize(): W3CElement = consumer.finalize()
}