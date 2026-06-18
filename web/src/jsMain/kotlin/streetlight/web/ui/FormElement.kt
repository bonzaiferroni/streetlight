package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV

fun AppScope.formBody(
    modifiers: ModifierSet? = null,
    block: DIV.() -> Unit
) = column(modify(modifiers, Gap8)) {
    block()
}

fun TagScope.formSection(
    name: String,
    modifiers: ModifierSet? = null,
    block: TagScope.() -> Unit
) = section {
    addModifiers(modifiers)
    filigree {
        heading3(name)
    }
    block()
}

fun TagScope.formCard(
    modifiers: ModifierSet? = null,
    block: TagScope.() -> Unit,
) = card(FormMod.Card) {
    addModifiers(modifiers)
    block()
}

fun TagScope.formCardSection(
    name: String,
    modifiers: ModifierSet? = null,
    block: TagScope.() -> Unit
) = formSection(name, modifiers) {
    formCard(null, block)
}

fun TagScope.formPart(
    instructions: String? = null,
    examples: List<String>? = null,
    bullets: List<String>? = null,
    fieldsFlex: Modifier = Flex1,
    info: TagScope.() -> Unit = {},
    fields: TagScope.() -> Unit,
) = column(FormMod.Part) {
    column(FormMod.Instructions) {
        instructions?.let {
            textBlock(it)
        }
        examples?.let {
            column(modify(Gap0, OpacityHigh)) {
                textBlock("Examples:")
                bulletsOf(it)
            }
        }
        bullets?.let {
            bulletsOf(FormMod.Bullets, it)
        }
        info(this@formPart)
    }
    column(modify(FormMod.Controls, fieldsFlex)) {
        fields(this@formPart)
    }
}

fun AppScope.formTextField(
    label: String,
    onValue: (String) -> Unit,
    flow: Flow<String?>,
    modifiers: ModifierSet? = null,
    footnote: String? = null,
    maxLength: Int? = null
) = column(modifiers) {
    textField(label, onValue = onValue, flow = flow)
    if (footnote != null || maxLength != null) {
        row(modify(OpacityHigh, Italic, WhiteSpaceNoWrap, PaddingX1, SmallText)) {
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

fun AppScope.formSubmit(
    label: String,
    onSubmit: () -> Unit,
    modifiers: ModifierSet? = null,
    messages: MessageStore? = null,
    back: LabeledAction? = null,
) {
    row {
        addModifiers(modifiers, JustifyContentSpaceBetween)
        row(modify(Flex1)) {
            back?.let {
                button(it.label, it.modifiers ?: modify(Secondary), it.onClick)
            }
        }
        row {
            messages?.let {
                messageBox(messages)
            }
            val element = button(label, onClick = onSubmit)
            messages?.let {
                element.flowIsWorking(it.isWorkingFlow, parentScope)
            }
        }
    }
}

object FormMod {
    val Card = modify(ZenBg, QueryContainer, Gap3)
    val Part = modify(ContainerMdRow)
    val Instructions = modify(Flex1, JustifyContentCenter, Margin1)
    val Controls = modify(JustifyContentCenter)
    val Bullets = modify(OpacityHigh)
    val GeoMap = modify(Height48, BorderRadius2, OverflowClip, MoonShadow)
}

data class LabeledAction(
    val label: String,
    val onClick: () -> Unit,
    val modifiers: ModifierSet? = null,
)