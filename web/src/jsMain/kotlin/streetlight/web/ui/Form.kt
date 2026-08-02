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
import koala.model.Field
import koala.model.MutableField
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
    card(modify(ZenBg, Gap2)) {
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
) = row(modify(Form.Row, mod), content = content)

fun ViewScope.formHeading(
    text: String
) = filigree {
    heading5(text, modify(OpacityHigh, TextTransformUppercase))
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
    buttonText: String = "submit",
    onClick: () -> Unit,
    messages: MessageStore? = null,
    onCancel: (() -> Unit)? = null,
    enabledField: Field<Boolean>? = null,
    isDisplayedFlow: Field<Boolean>? = null,
) = row(mod = modify(AlignItemsStart, JustifyContentEnd)) {
    messages?.let {
        messageBox(it)
    }
    column {
        val button = button(buttonText, onClick, modify(Accent))
        enabledField?.let {
            configureEnabledFlow(button, enabledField)
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
) = textBlock(text, mod = modify(mod, TextAlignCenter, OpacityHigh))

fun ViewScope.formField(
    mod: ModifierSet? = null,
    block: ViewScope.() -> Unit
) = column(mod) {
    block()
}

fun ViewScope.formTextField(
    field: MutableField<String>,
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