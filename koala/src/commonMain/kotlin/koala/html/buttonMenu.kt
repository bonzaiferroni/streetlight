package koala.html

import koala.css.*
import kotlinx.html.DIV
import kotlinx.html.FlowContent

fun FlowContent.buttonMenu(
    label: String,
    id: Id = Id("${label}-menu"),
    modifiers: ModifierSet? = null,
    menuModifiers: ModifierSet? = null,
    block: DIV.() -> Unit = {}
) {
    val anchor = Anchor("${id}-anchor")

    popover(id, anchor, modify(menuModifiers, Magic, SlideUp)) {
        block()
    }
    button("☰ $label", modifiers) {
        setAnchor(anchor)
        setAttribute(Attribute.PopoverTarget, id.identifier)
    }
}