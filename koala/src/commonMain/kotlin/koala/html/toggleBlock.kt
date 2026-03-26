package koala.html

import koala.css.Css
import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.div

fun FlowContent.toggleBlock(
    label: String,
    modifiers: ModifierSet? = null,
    base: FlowContent.() -> Unit,
    cover: FlowContent.() -> Unit,
    block: (DIV.() -> Unit)? = null,
) {
    div {
        configureToggleBlock(label, modifiers, base, cover, block)
    }
}

fun DIV.configureToggleBlock(
    label: String,
    modifiers: ModifierSet? = null,
    base: FlowContent.() -> Unit,
    cover: FlowContent.() -> Unit,
    block: (DIV.() -> Unit)? = null,
) {
    addModifiers(ToggleBlock.root, modifiers)
    div {
        addModifiers(ToggleBlock.base)
        base()
    }
    div {
        addModifiers(ToggleBlock.cover)
        cover()
    }
    switch(label)

    block?.invoke(this)
}

object ToggleBlock {
    val root = Css("toggle-block")
    val cover = Css("toggle-block__cover")
    val base = Css("toggle-block__base")
}