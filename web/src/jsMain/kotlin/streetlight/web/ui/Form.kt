package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.bulletsOf
import koala.html.heading5
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
    vararg bullets: String
) = bulletsOf(Form.bulletsMod, *bullets)

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