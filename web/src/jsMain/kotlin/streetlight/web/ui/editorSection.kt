package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import streetlight.model.data.GalaxyEdit

fun DOMContext.editorSection(
    name: String,
    modifiers: ModifierSet? = null,
    block: DOMContext.() -> Unit
) {
    section {
        addModifiers(modifiers)
        filigree {
            heading3(name)
        }
        card(EditorMod.Card) {
            block()
        }
    }
}

fun DOMContext.editorPart(
    instructions: String? = null,
    examples: List<String>? = null,
    bullets: List<String>? = null,
    info: DOMContext.() -> Unit = {},
    fields: DOMContext.() -> Unit,
) = column(EditorMod.Part) {
    column(EditorMod.Instructions) {
        instructions?.let {
            textBlock(it)
        }
        examples?.let {
            column(modify(Gap0, OpacityMost)) {
                textBlock("Examples:")
                bulletsOf(it)
            }
        }
        bullets?.let {
            bulletsOf(modify(OpacityMost), it)
        }
        info(this@editorPart)
    }
    column(EditorMod.Fields) {
        fields(this@editorPart)
    }
}

fun RenderContext.editorTextField(
    label: String,
    onValue: (String) -> Unit,
    flow: Flow<String>,
    modifiers: ModifierSet? = null,
    footnote: String? = null,
    maxLength: Int? = null
) = column(modifiers) {
    textField(label, onValue = onValue, flow = flow)
    if (footnote != null || maxLength != null) {
        row(modify(OpacityMost, Italic, WhiteSpaceNoWrap, PaddingX1, SmallText)) {
            footnote?.let {
                textBlock(footnote)
            }
            maxLength?.let {
                flowBlock(flow, modify(MarginLeftAuto)) { text ->
                    textBlock("${text.length}/$maxLength")
                }
            }
        }
    }
}

object EditorMod {
    val Card = modify(ZenBg, QueryContainer, Gap3)
    val Part = modify(ContainerMdRow)
    val Instructions = modify(Flex1, JustifyContentCenter, Margin1)
    val Fields = modify(Flex1)
}