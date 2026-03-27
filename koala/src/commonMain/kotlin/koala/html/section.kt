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
        addModifiers(SectionKey.Class, modifiers)
        block()
    }
}

object SectionKey {
    val Class = Css("section")
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