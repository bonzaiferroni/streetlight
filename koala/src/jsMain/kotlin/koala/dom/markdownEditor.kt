package koala.dom

import koala.css.ModifierSet
import koala.css.Width100
import koala.css.applyModifiers
import koala.external.SimpleMDE
import koala.external.SimpleMDEOptions
import koala.html.Id
import koala.html.applyBlockLabel
import koala.html.applyId
import kotlinx.html.dom.append
import kotlinx.html.js.div
import kotlinx.html.js.textArea

fun RenderContext.markdownEditor(
    modifiers: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = null,
) {
    val element = textArea {
        applyModifiers(Width100, modifiers)
        applyId(id)

        placeholder?.let {
            this.placeholder = it
        }
    }

    val editor = SimpleMDE(
        SimpleMDEOptions(
            element = element,

        )
    )
}