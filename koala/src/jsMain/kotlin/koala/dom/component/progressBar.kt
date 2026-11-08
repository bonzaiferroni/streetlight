package koala.dom.component

import kampfire.model.Tap
import koala.component.ProgressBarStyle
import koala.dom.ViewScope
import koala.modifier.*
import kotlinx.coroutines.launch
import web.html.HTMLDivElement
import web.html.HTMLElement

/**
 * A bar filled to [progressState], from 0 to 1, following each change. It eases into each change unless
 * [isSmooth] is off, as for progress set every frame, which an easing restarted each frame would leave behind.
 */
fun ViewScope.progressBar(
    progressState: Tap<Float>,
    mod: Modifier? = modify(Height(2), MinWidth(16)),
    barMod: Modifier? = null,
    isSmooth: Boolean = true,
): HTMLDivElement {
    val element = div(mod) {
        div(modify(barMod, ProgressBarStyle.Indicator))
    }
    if (!isSmooth) element.firstElementChild?.unsafeCast<HTMLElement>()?.style?.setProperty("transition", "none")
    fun display(progress: Float) = element.setStyle(ProgressBarStyle.Progress.of(progress.coerceIn(0f, 1f)))
    display(progressState.now)
    contentScope.launch { progressState.flow.collect { display(it) } }
    return element
}
