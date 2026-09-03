package koala.html

import koala.css.*
import kotlinx.html.FlowContent
import kotlinx.html.SECTION
import kotlinx.html.section as sectionTag

fun FlowContent.section(
    modifiers: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) {
    sectionTag {
        addModifiers(SectionStyle.Class, modifiers)
        block()
    }
}

fun FlowContent.section(
    id: Id,
    modifiers: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) {
    section(modifiers) {
        setId(id)
        block()
    }
}

fun FlowContent.section(
    title: String,
    modifiers: ModifierSet? = null,
    block: SECTION.() -> Unit = {}
) {
    section(modifiers) {
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
val SectionCss get() = """
.section {
    display: flex;
    flex-direction: column;
    min-width: 0;
    gap: var(--unit-spacing);
}
"""