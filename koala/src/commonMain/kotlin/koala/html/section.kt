package koala.html

import koala.modifier.*
import kotlinx.html.FlowContent
import kotlinx.html.SECTION
import kotlinx.html.section as sectionTag

fun FlowContent.section(
    mod: Modifier? = null,
    block: SECTION.() -> Unit = {}
) {
    sectionTag {
        addModifiers(SectionStyle.Class, mod)
        block()
    }
}

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