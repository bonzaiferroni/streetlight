package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV

fun RenderContext.formBody(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
) = column(modify(modifiers, Gap8)) {
    block()
}

fun DOMContext.formSection(
    name: String,
    modifiers: ModifierSet? = null,
    block: DOMContext.() -> Unit
) = section {
    addModifiers(modifiers)
    filigree {
        heading3(name)
    }
    block()
}

fun DOMContext.formCard(
    modifiers: ModifierSet? = null,
    block: DOMContext.() -> Unit,
) = card(FormMod.Card) {
    addModifiers(modifiers)
    block()
}

fun DOMContext.formCardSection(
    name: String,
    modifiers: ModifierSet? = null,
    block: DOMContext.() -> Unit
) = formSection(name, modifiers) {
    formCard(null, block)
}

fun DOMContext.formPart(
    instructions: String? = null,
    examples: List<String>? = null,
    bullets: List<String>? = null,
    fieldsFlex: Modifier = Flex1,
    info: DOMContext.() -> Unit = {},
    fields: DOMContext.() -> Unit,
) = column(FormMod.Part) {
    column(FormMod.Instructions) {
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
            bulletsOf(FormMod.Bullets, it)
        }
        info(this@formPart)
    }
    column(modify(fieldsFlex)) {
        fields(this@formPart)
    }
}

fun RenderContext.formTextField(
    label: String,
    onValue: (String) -> Unit,
    flow: Flow<String?>,
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
                    textBlock("${text?.length ?: 0}/$maxLength")
                }
            }
        }
    }
}

object FormMod {
    val Card = modify(ZenBg, QueryContainer, Gap3)
    val Part = modify(ContainerMdRow)
    val Instructions = modify(Flex1, JustifyContentCenter, Margin1)
    val Bullets = modify(OpacityMost)
    val GeoMap = modify(Height48, BorderRadius2, OverflowClip, MoonShadow)
}