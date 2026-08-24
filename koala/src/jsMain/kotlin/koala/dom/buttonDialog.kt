package koala.dom

import koala.css.*
import koala.model.storeOf
import koala.model.toggle

fun ViewScope.buttonDialog(
    label: String,
    modifiers: ModifierSet? = null,
    dialogModifiers: ModifierSet? = null,
    emoji: String = "👀",
    block: ViewScope.() -> Unit
) {
    val isOpen = storeOf(false)

    dialog(isOpen, dialogModifiers) {
        dialogContent(label) {
            block()
        }
    }

    button("$emoji $label", isOpen::toggle, modifiers)
}