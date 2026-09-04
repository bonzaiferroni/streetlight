package streetlight.web.ui

import koala.Lottie
import koala.css.*
import koala.dom.*
import koala.dom.textBlock
import koala.html.bulletsOf
import koala.html.column
import koala.html.filigree
import koala.html.heading3
import koala.html.heading4
import koala.html.textBlock
import koala.dom.MenuAction
import kampfire.model.Tap
import kampfire.model.MutableTap
import kotlinx.html.DIV
import kotlinx.html.SECTION

fun ViewScope.formColumn(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit,
) = column(mod = modify(mod, Gap2), content = content)

fun ViewScope.formCard(
    name: String?,
    mod: ModifierSet? = null,
    content: DIV.() -> Unit,
) = column(mod = mod) {
    name?.let {
        filigree {
            heading3(name)
        }
    }
    card(modify(ZenBg, Gap2, Outline, Padding2)) {
        content()
    }
}

fun ViewScope.formSection(
    name: String? = null,
    mod: ModifierSet? = null,
    content: SECTION.() -> Unit
) = section(mod) {
    name?.let {
        formHeading(name)
    }
    content()
}

fun ViewScope.formRow(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit
) = row(modify(BodyStyle.FormRow, mod), content = content)

fun ViewScope.formHeading(
    text: String
) = filigree(ruleMaxWidth = MaxWidth32) {
    heading4(text, modify(SystemFg))
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

fun ViewScope.formSubmit(
    label: String = "submit",
    onClick: () -> Unit,
    messenger: MessageStore? = null,
    buttonMod: ModifierSet = modify(Primary),
    enabledTap: Tap<Boolean>? = null,
    isDisplayedFlow: Tap<Boolean>? = null,
    back: MenuAction? = null,
) = row(mod = modify(AlignItemsStart)) {
    row(modify(Flex1)) {
        back?.let {
            button(it.label, it.onClick, it.mod ?: modify(Secondary))
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

fun ViewScope.formText(
    text: String,
    mod: ModifierSet? = null,
) = textBlock(text, mod = modify(mod, TextAlignCenter, OpacityHigh, WhiteSpacePreLine))

fun ViewScope.formField(
    mod: ModifierSet? = null,
    block: ViewScope.() -> Unit
) = column(mod) {
    block()
}

fun ViewScope.formTextField(
    field: MutableTap<String>,
    label: String? = null,
    mod: ModifierSet? = null,
    footnote: String? = null,
    placeholder: String? = label,
    maxLength: Int? = null
) = column(mod) {
    textField(field, label, placeholder = placeholder, maxLength = maxLength)
    footnote?.let {
        textBlock(footnote, modify(OpacityHigh, Italic, WhiteSpaceNoWrap, PaddingX1, TextSmall))
    }
}

fun ViewScope.formFiller(
    lottie: Lottie
) = lottie(lottie, modify(MaxHeight16))