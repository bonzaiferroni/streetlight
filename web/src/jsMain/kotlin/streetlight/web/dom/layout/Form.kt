package streetlight.web.dom.layout

import koala.SvgPack
import koala.Lottie
import koala.modifier.*
import koala.dom.*
import koala.component.textBlock
import koala.component.bulletsOf
import koala.component.column
import koala.component.filigree
import koala.component.heading3
import koala.component.heading4
import koala.model.MenuAction
import kampfire.model.Tap
import kampfire.model.MutableTap
import kotlinx.html.DIV
import kotlinx.html.SECTION
import koala.dom.layout.*
import koala.dom.component.*
import koala.model.*
import streetlight.web.layout.BodyStyle

/** The column of a form's cards. */
fun ViewScope.formColumn(
    mod: Modifier? = null,
    content: DIV.() -> Unit,
) = column(mod = modify(mod, Gap(2)), content = content)

/** A card of form fields under the heading [name]. */
fun ViewScope.formCard(
    name: String?,
    mod: Modifier? = null,
    content: DIV.() -> Unit,
) = column(mod = mod) {
    name?.let {
        filigree {
            heading3(name)
        }
    }
    card(modify(ZenBg, Gap(2), Outline, Padding(2))) {
        content()
    }
}

fun ViewScope.formSection(
    name: String? = null,
    mod: Modifier? = null,
    content: SECTION.() -> Unit
) = section(mod) {
    name?.let {
        formHeading(name)
    }
    content()
}

fun ViewScope.formRow(
    mod: Modifier? = null,
    content: DIV.() -> Unit
) = row(modify(BodyStyle.ColumnRow, mod), content = content)

fun ViewScope.formHeading(
    text: String
) = filigree(ruleMod = MaxWidth(32)) {
    heading4(text, modify(SystemFg, TextUppercase, MoonShadowText))
}

fun ViewScope.formBullets(
    heading: String?,
    vararg bullets: String,
) = column(modify(Gap0, OpacityHigh)) {
    heading?.let {
        textBlock(it)
    }
    bulletsOf(FormMod.Bullets, *bullets)
}

/**
 * The submit row of a form: the [back] action, the messages of [messenger], and the submit button.
 *
 * [enabledTap] enables the button and [isDisplayedFlow] shows it.
 */
fun ViewScope.formSubmit(
    label: String = "submit",
    onClick: () -> Unit,
    messenger: MessageStore? = null,
    buttonMod: Modifier = Primary,
    enabledTap: Tap<Boolean>? = null,
    isDisplayedFlow: Tap<Boolean>? = null,
    back: MenuAction? = null,
) = row(mod = AlignItemsStart) {
    row(Flex1) {
        back?.let {
            button(it.label, it.onClick, it.mod ?: Secondary)
        }
    }
    messenger?.let {
        messageBox(it)
    }
    column {
        val button = button(label, onClick, buttonMod)
        enabledTap?.let {
            configureEnabledFlow(button, enabledTap)
        }
        isDisplayedFlow?.let {
            button.flowIsDisplayed(it, contentScope)
        }
    }
}

fun ViewScope.centeredText(
    text: String,
    mod: Modifier? = null,
) = textBlock(text, mod = modify(mod, TextAlignCenter, OpacityHigh, WhiteSpacePreLine))

fun ViewScope.formField(
    mod: Modifier? = null,
    block: ViewScope.() -> Unit
) = column(mod) {
    block()
}

/** A text field with an optional [footnote] beneath. */
fun ViewScope.formTextField(
    field: MutableTap<String>,
    label: String? = null,
    mod: Modifier? = null,
    footnote: String? = null,
    placeholder: String? = label,
    maxLength: Int? = null,
    icon: SvgPack? = null,
) = column(mod) {
    textField(field, label, placeholder = placeholder, maxLength = maxLength, icon = icon)
    footnote?.let {
        textBlock(footnote, modify(OpacityHigh, Italic, WhiteSpaceNoWrap, PaddingX1, TextSmall))
    }
}

/** An animation that fills the space beside a form. */
fun ViewScope.formFiller(
    lottie: Lottie
) = lottie(lottie, MaxHeight(16))