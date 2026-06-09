package koala.dom

import koala.css.*
import koala.model.storeOf

fun RenderContext.buttonDialog(
    label: String,
    modifiers: ModifierSet? = null,
    dialogModifiers: ModifierSet? = null,
    emoji: String = "👀",
    block: RenderContext.() -> Unit
) {
    val isOpen = storeOf(false)

    dialogBox(label, isOpen.flow, dialogModifiers, onClose = { isOpen.set { false } }) {
        block()
    }

    button("$emoji $label", modifiers, onClick = { isOpen.set { !isOpen.now } })
}