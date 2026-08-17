package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.dom.textBlock
import koala.html.bulletsOf
import koala.html.column
import koala.html.filigree
import koala.html.heading3
import koala.html.heading5
import koala.html.textBlock
import koala.model.Tap
import koala.model.MutableTap
import kotlinx.html.DIV
import kotlinx.html.SECTION

fun ViewScope.formColumn(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit,
) = column(mod = modify(mod, Gap2), content = content)

fun ViewScope.formCard(
    name: String,
    mod: ModifierSet? = null,
    content: DIV.() -> Unit,
) = column(mod = mod) {
    filigree {
        heading3(name)
    }
    card(modify(ZenBg, Gap2, Outline, Padding2)) {
        content()
    }
}

fun ViewScope.formSection(
    name: String? = null,
    content: SECTION.() -> Unit
) = section {
    name?.let {
        formHeading(name)
    }
    content()
}

fun ViewScope.formRow(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit
) = row(modify(BodyStyle.FlexGrid2, mod), content = content)

fun ViewScope.formHeading(
    text: String
) = filigree(ruleMaxWidth = MaxWidth32) {
    heading5(text, BodyStyle.LabelHeading)
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
    onSubmit: () -> Unit,
    messenger: MessageStore? = null,
    buttonMod: ModifierSet = modify(Accent),
    onCancel: (() -> Unit)? = null,
    enabledTap: Tap<Boolean>? = null,
    isDisplayedFlow: Tap<Boolean>? = null,
    back: LabeledAction? = null,
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
        val button = button(label, onSubmit, buttonMod)
        enabledTap?.let {
            configureEnabledFlow(button, enabledTap)
        }
        isDisplayedFlow?.let {
            button.flowIsDisplayed(it, contentScope)
        }
        onCancel?.let {
            button("cancel", it, modify(Zen))
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