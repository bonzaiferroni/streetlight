package koala.dom

import koala.css.ModifierSet
import koala.css.Width100P
import koala.css.addModifiers
import koala.external.SimpleMDE
import koala.external.SimpleMDEOptions
import koala.html.Id
import koala.html.setId
import kotlinx.html.js.textArea

fun AppScope.markdownEditor(
    modifiers: ModifierSet? = null,
    id: Id? = null,
    placeholder: String? = null,
) {
    val element = textArea {
        addModifiers(Width100P, modifiers)
        setId(id)

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