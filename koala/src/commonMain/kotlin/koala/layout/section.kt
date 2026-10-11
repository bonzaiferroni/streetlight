package koala.layout

import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.SECTION
import kotlinx.html.section as sectionTag
import koala.component.filigree
import koala.component.heading3
import koala.page.Id
import koala.page.setId

/** A `section` laid out as a flex column. */
fun FlowContent.section(
    mod: Modifier? = null,
    block: SECTION.() -> Unit = {}
) {
    sectionTag {
        addModifiers(SectionStyle.Class, mod)
        block()
    }
}

/** A [section] with [id]. */
fun FlowContent.section(
    id: Id,
    mod: Modifier? = null,
    block: SECTION.() -> Unit = {}
) {
    section(mod) {
        setId(id)
        block()
    }
}

/** A [section] opening with [title] in a [filigree]. */
fun FlowContent.section(
    title: String,
    mod: Modifier? = null,
    block: SECTION.() -> Unit = {}
) {
    section(mod) {
        filigree {
            heading3(title)
        }
        block()
    }
}

object SectionStyle {
    val Class = Class("section")
}

// language="CSS"
val SectionCss get() = with(SectionStyle) { """
$Class {
    display: flex;
    flex-direction: column;
    min-width: 0;
    gap: var(--unit);
}
""" }