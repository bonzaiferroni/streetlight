package streetlight.web

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.dom.addClass
import kotlinx.dom.clear
import kotlinx.html.FlowContent
import kotlinx.html.Tag
import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.HTMLElement

class AltRenderContext(
    val parent: FlowContent,
    val renderScope: CoroutineScope,
    val app: AppContext,
): FlowContent by parent {

    fun <T> renderState(
        flow: Flow<T>,
        animate: Boolean = false,
        block: AltRenderContext.(T) -> HTMLElement
    ) {
        var lastContainer: HTMLElement? = null
        var job: Job? = null

        renderScope.launch {
            var currentValue: T? = null
            flow.collect {  value ->
                if (value == currentValue) return@collect
                job?.cancel()
                job = SupervisorJob()
                currentValue = value

                if (animate) lastContainer?.exitStage()
                else lastContainer?.remove()

                val scope = CoroutineScope(Dispatchers.Main + job)
                val childContext = AltRenderContext(parent, scope, app)
                lastContainer = childContext.block(value)
                if (animate) {
                    lastContainer.addClass("state-render-animation")
                    lastContainer.enterStage()
                }
            }
        }
    }
}

fun HTMLElement.renderAltRoot(
    scope: CoroutineScope,
    app: AppContext,
    block: AltRenderContext.() -> Unit
) {
    clear()
    append {
        div {
            AltRenderContext(this, scope, app).block()
        }
    }
}