package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.style

fun FlowContent.wireBlock(
    id: Id,
    modifiers: ModifierSet? = null,
    isVisibleWhileLoading: Boolean = false,
) {
    div {
        applyId(id)
        applyModifiers(ElementClass.wireBlock, modifiers)
        if (!isVisibleWhileLoading) {
            style = "display: none;"
        }
    }
}