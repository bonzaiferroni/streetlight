package koala.dom

import koala.css.*
import koala.html.ButtonPopover
import koala.model.storeOf
import kotlinx.html.FlowContent

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