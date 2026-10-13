package koala.dom.component

import koala.modifier.*
import koala.component.filigree
import koala.component.heading3
import kotlinx.html.SECTION
import kotlinx.html.js.section as sectionTag
import koala.dom.AppendScope
import koala.component.SectionStyle

/** A `section` laid out as a flex column. */
fun AppendScope.section(
    mod: Modifier? = null,
    block: SECTION.() -> Unit = {}
) = sectionTag {
    addModifiers(SectionStyle.Class, mod)
    block()
}

/** A [section] opening with [title] in a filigree. */
fun AppendScope.section(
    title: String,
    mod: Modifier? = null,
    block: SECTION.() -> Unit = {}
) = section(mod) {
    filigree {
        heading3(title)
    }
    block()
}