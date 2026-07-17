package koala.dom

import koala.css.*
import koala.model.storeOf

fun ViewScope.buttonDialog(
    label: String,
    modifiers: ModifierSet? = null,
    dialogModifiers: ModifierSet? = null,
    emoji: String = "👀",
    block: ViewScope.() -> Unit
) {
    val isOpen = storeOf(false)

    dialog(label, isOpen.flow, dialogModifiers, onClose = { isOpen.set { false } }) {
        block()
    }

    button("$emoji $label", { isOpen.set { !isOpen.now } }, modifiers)
}