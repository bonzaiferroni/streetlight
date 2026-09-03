package koala.dom

import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.SectionStyle
import koala.html.filigree
import koala.html.heading3
import kotlinx.html.SECTION
import kotlinx.html.js.section as sectionTag

fun AppendScope.section(
    mod: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) = sectionTag {
    addModifiers(SectionStyle.Class, mod)
    block()
}

fun AppendScope.section(
    title: String,
    mod: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) = section(mod) {
    filigree {
        heading3(title)
    }
    block()
}