package koala.dom

import koala.css.*

fun RenderContext.setImage(
    modifiers: ModifierSet? = null
) {
    box(modify(SetImageClass.parent, modifiers)) {
        box(modify(SetImageClass.placeholder))
    }
}

fun RenderContext.setImage(
    label: String,
    modifiers: ModifierSet? = null
) {
    blockLabel(label, modifiers) {
        setImage(modify(Size100))
    }
}


object SetImageClass {
    val parent = Css("set-image")
    val placeholder = Css("set-image-placeholder")
}