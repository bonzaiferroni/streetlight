package koala.dom

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.removeClass
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement
import kotlin.collections.first

typealias DOMContext = TagConsumer<HTMLElement>

interface AltRenderContext: DOMContext {

    val renderScope: CoroutineScope

    fun <State> renderState(
        flow: Flow<State>,
        animate: Boolean = false,
        configureParent: (DIV.() -> Unit)? = null,
        configureContainer: (DIV.() -> Unit)? = null,
        cacheRenderedElements: Boolean = false,
        block: AltRenderContext.(State) -> Unit
    ) {
        val parent = div("state-render") {
            configureParent?.invoke(this)
        }
        var render: HTMLElement? = null
        var job: Job? = null
        var renderedOnce = false
        val renderCache = mutableMapOf<State, HTMLElement>()

        renderScope.launch {
            var currentValue: State? = null
            flow.collect {  value ->
                if (renderedOnce && value == currentValue) return@collect
                renderedOnce = true
                if (!cacheRenderedElements) job?.cancel()
                currentValue = value

                render?.hide(animate)

                render = renderCache[value]?.also {
                    parent.append(it)
                } ?: parent.append {
                    val container = div() {
                        configureContainer?.invoke(this)
                    }
                    container.append {
                        val consumer = this
                        job = SupervisorJob()
                        val scope = CoroutineScope(Dispatchers.Main + job)
                        val context = object: AltRenderContext, DOMContext by consumer {
                            override val renderScope = scope
                        }
                        // RenderContext(this, scope, app).block(value)
                         context.block(value)
                    }
                    if (cacheRenderedElements) renderCache[value] = container
                }.first()

                render.show(animate)

                if (animate) {
                    val height = render.scrollHeight
                    parent.style.height = "${height}px"
                }
            }
        }
    }

    fun HTMLElement.hide(animate: Boolean) {
        if (animate) {
            addClass("exit-stage")
            removeClass("enter-stage")
            renderScope.launch {
                delay(250)
                remove()
                removeClass("exit-stage")
            }
        } else {
            remove()
        }
    }

    fun HTMLElement.show(animate: Boolean) {
        if (animate) {
            addClass("state-render-animation")
            renderScope.launch {
                delay(200)
                addClass("enter-stage")
            }
        }
    }
}