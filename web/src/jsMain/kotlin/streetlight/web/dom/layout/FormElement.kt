package streetlight.web.dom.layout

import koala.modifier.*
import koala.dom.*
import koala.component.bulletsOf
import koala.component.filigree
import koala.component.heading3
import koala.model.MenuAction
import kotlinx.html.DIV
import koala.dom.layout.*
import koala.dom.component.*
import koala.model.*
import streetlight.web.utility.element

fun ViewScope.formBodyProto(
    mod: Modifier? = null,
    block: DIV.() -> Unit
) = column(modify(mod, Gap(8))) {
    block()
}

fun AppendScope.formSectionLegacy(
    name: String,
    mod: Modifier? = null,
    block: AppendScope.() -> Unit
) = section {
    addModifiers(mod)
    filigree {
        heading3(name)
    }
    block()
}

fun AppendScope.formCard(
    mod: Modifier? = null,
    block: AppendScope.() -> Unit,
) = card(FormMod.Card) {
    addModifiers(mod)
    block()
}

@Deprecated("use formSection")
fun AppendScope.formCardSection(
    name: String,
    mod: Modifier? = null,
    block: AppendScope.() -> Unit
) = formSectionLegacy(name, mod) {
    formCard(null, block)
}

@Deprecated("use formField")
fun AppendScope.formPart(
    instructions: String? = null,
    bullets: List<String>? = null,
    bulletsHeading: String? = null,
    fieldsFlex: Modifier = Flex1,
    info: AppendScope.() -> Unit = {},
    fields: AppendScope.() -> Unit,
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

@Deprecated("Use formSubmit")
fun ViewScope.formSubmitLegacy(
    label: String,
    onSubmit: () -> Unit,
    mod: Modifier? = null,
    messages: MessageStore? = null,
    back: MenuAction? = null,
) {
    row {
        addModifiers(mod, JustifyContentSpaceBetween)
        row(Flex1) {
            back?.let {
                button(it.label, it.onClick, it.mod ?: Secondary)
            }
        }
        row {
            messages?.let {
                messageBox(messages)
            }
            val element = button(label, onSubmit, Accent)
            messages?.let {
                element.flowIsWorking(it.isWorkingFlow, contentScope)
            }
        }
    }
}

object FormMod {
    val Card = modify(ZenBg, ContainerTypeInlineSize, Gap(3), Padding(2), Outline)
    val Part = modify(ContainerMdRow)
    val Instructions = modify(Flex1, JustifyContentCenter, Margin1)
    val Controls = modify(JustifyContentCenter)
    val Bullets = modify(TextSmall)
    val GeoMap = modify(Height(48), BorderRadius2, OverflowClip, MoonShadow)
}

