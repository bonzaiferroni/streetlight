package koala.dom

import koala.modifier.*
import kampfire.model.storeOf
import kampfire.model.toggle

/** A button labeled [label] that opens a dialog holding what [block] builds. */
fun ViewScope.buttonDialog(
    label: String,
    mod: Modifier? = null,
    dialogMod: Modifier? = null,
    emoji: String = "👀",
    block: ViewScope.() -> Unit
) {
    val isOpen = storeOf(false)

    dialog(isOpen, dialogMod) {
        dialogContent(label) {
            block()
        }
    }

    button("$emoji $label", isOpen::toggle, mod)
}