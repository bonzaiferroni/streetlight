package koala.dom

import koala.modifier.*
import koala.html.SectionStyle
import koala.html.filigree
import koala.html.heading3
import kotlinx.html.SECTION
import kotlinx.html.js.section as sectionTag

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