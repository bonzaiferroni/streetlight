package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.column
import koala.html.heading5
import koala.html.textBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.html.DIV
import kotlinx.html.FlowContent
import kotlinx.html.SECTION

fun AppScope.form(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit,
) = column(mod = modify(mod, Gap2), content = content)

fun AppScope.formSection(
    name: String? = null,
    content: SECTION.() -> Unit
) = section {
    name?.let {
        formHeading(name)
    }
    content()
}

fun AppScope.formRow(
    mod: ModifierSet? = null,
    content: DIV.() -> Unit
) = row(modify(Form.Row, mod), content = content)

fun AppScope.formHeading(
    text: String
) = filigree {
    heading5(text, modify(OpacityHigh, TextTransformUppercase))
}

fun FlowContent.formBullets(
    heading: String?,
    vararg bullets: String,
) = column(modify(Gap0, OpacityHigh)) {
    heading?.let {
        textBlock(it)
    }
    bulletsOf(FormMod.Bullets, *bullets)
}

fun AppScope.formSubmit(
    buttonText: String = "submit",
    onClick: () -> Unit,
    messages: MessageStore? = null,
    enabledFlow: Flow<Boolean>? = null,
) = row(mod = modify(AlignItemsStart, JustifyContentEnd)) {
    messages?.let {
        messageBox(it)
    }
    val button = button(buttonText, onClick, modify(Accent))
    enabledFlow?.let {
        configureEnabledFlow(button, enabledFlow)
    }
}

fun AppScope.formText(
    text: String,
    mod: ModifierSet? = null,
) = textBlock(text, mod = modify(mod, TextAlignCenter, OpacityHigh))