package koala.dom

import koala.css.ModifierSet
import koala.css.Selected
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

fun <State> RenderContext.selectionBlock(
    flow: Flow<List<State>>,
    onSelect: (State) -> Unit,
    selectFlow: Flow<State?>,
    modifiers: ModifierSet? = null,
    renderCacheCount: Int? = null,
    config: (DIV.() -> Unit)? = null,
    block: RenderContext.(State) -> HTMLElement
): HTMLDivElement {
    val element = flowBlock(flow) { items ->
        column {
            items.forEach { item ->
                block(item).onClick { element ->
                    element.modify(Selected)
                }
            }
        }
    }

    return element
}