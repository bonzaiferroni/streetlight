package koala.dom

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.Reveal
import koala.html.Queryable
import koala.html.ToggleBlock
import koala.html.configureToggleBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div
import org.w3c.dom.HTMLElement

fun RenderContext.toggleBlock(
    label: String,
    modifiers: ModifierSet? = null,
    base: FlowContent.() -> Unit,
    cover: FlowContent.() -> Unit,
    block: (DIV.() -> Unit)? = null,
): HTMLElement {
    val element = div {
        configureToggleBlock(label, modifiers, base, cover, block)
    }
    error("not implemented")
}

fun RenderContext.wireToggleBlock(
    root: HTMLElement,
    initialOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    bindFlow: Flow<Boolean>? = null,
) {
    // val cover = root.querySelector(ToggleBlock.cover) ?: error("cover not found")
    // val base = root.querySelector(ToggleBlock.base) ?: error("base not found")
    console.log("wiring")
    val switchElement = queryAndWireSwitch(root, onToggle = {
        console.log("ey")
        if (it) {
            root.modify(Reveal)
        } else {
            root.unmodify(Reveal)
        }
        onToggle?.invoke(it)
    })
}

fun RenderContext.queryAndWireToggleBlock(
    ancestor: HTMLElement,
    queryable: Queryable = ToggleBlock.root,
    initialOn: Boolean = false,
    onToggle: ((Boolean) -> Unit)? = null,
    bindFlow: Flow<Boolean>? = null,
) {
    val root = ancestor.querySelector(queryable) ?: error("toggle block not found")
    wireToggleBlock(root, initialOn, onToggle, bindFlow)
}

private val toggleBlockStyle = """

"""