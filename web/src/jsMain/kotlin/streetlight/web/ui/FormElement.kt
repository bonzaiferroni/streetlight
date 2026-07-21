package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.filigree
import koala.html.heading3
import koala.model.MutableField
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV

fun ViewScope.formBodyProto(
    mod: ModifierSet? = null,
    block: DIV.() -> Unit
) = column(modify(mod, Gap8)) {
    block()
}

fun TagScope.formSectionLegacy(
    name: String,
    mod: ModifierSet? = null,
    block: TagScope.() -> Unit
) = section {
    addModifiers(mod)
    filigree {
        heading3(name)
    }
    block()
}

fun TagScope.formCard(
    mod: ModifierSet? = null,
    block: TagScope.() -> Unit,
) = card(FormMod.Card) {
    addModifiers(mod)
    block()
}

fun TagScope.formCardSection(
    name: String,
    mod: ModifierSet? = null,
    block: TagScope.() -> Unit
) = formSectionLegacy(name, mod) {
    formCard(null, block)
}

fun TagScope.formPart(
    instructions: String? = null,
    bullets: List<String>? = null,
    bulletsHeading: String? = null,
    fieldsFlex: Modifier = Flex1,
    info: TagScope.() -> Unit = {},
    fields: TagScope.() -> Unit,
) = column(FormMod.Part) {
    column(FormMod.Instructions) {
        instructions?.let {
            textBlock(it)
        }
        bullets?.let { bullets ->
            column(modify(Gap0, OpacityHigh)) {
                bulletsHeading?.let {
                    textBlock(it)
                }
                bulletsOf(FormMod.Bullets, bullets)
            }
        }
        info(this@formPart)
    }
    column(modify(FormMod.Controls, fieldsFlex)) {
        fields(this@formPart)
    }
}

@Deprecated("use textField")
fun ViewScope.formTextField(
    field: MutableField<String>,
    label: String? = null,
    mod: ModifierSet? = null,
    footnote: String? = null,
    placeholder: String? = label,
    maxLength: Int? = null
) = column(mod) {
    textField(field, label, placeholder = placeholder, maxLength = maxLength)
    if (footnote != null || maxLength != null) {
        row(modify(OpacityHigh, Italic, WhiteSpaceNoWrap, PaddingX1, TextSmall)) {
            footnote?.let {
                textBlock(footnote)
            }
        }
    }
}

fun ViewScope.formSubmit(
    label: String,
    onSubmit: () -> Unit,
    mod: ModifierSet? = null,
    messages: MessageStore? = null,
    back: LabeledAction? = null,
) {
    row {
        addModifiers(mod, JustifyContentSpaceBetween)
        row(modify(Flex1)) {
            back?.let {
                button(it.label, it.onClick, it.mod ?: modify(Secondary))
            }
        }
        row {
            messages?.let {
                messageBox(messages)
            }
            val element = button(label, onSubmit, modify(Accent))
            messages?.let {
                element.flowIsWorking(it.isWorkingFlow, contentScope)
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
    val mod: ModifierSet? = null,
)