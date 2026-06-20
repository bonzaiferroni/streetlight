package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.SectionKey
import koala.html.filigree
import koala.html.heading3
import kotlinx.html.SECTION
import kotlinx.html.js.section as sectionTag

fun TagScope.section(
    modifiers: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) = sectionTag {
    addModifiers(SectionKey.Class, modifiers)
    block()
}

fun TagScope.section(
    title: String,
    modifiers: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) = section(modifiers) {
    filigree {
        heading3(title)
    }
    block()
}