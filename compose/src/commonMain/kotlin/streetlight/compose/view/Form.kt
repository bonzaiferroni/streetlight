package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import kampfire.model.MutableTap
import kampfire.model.Tap
import kampfire.model.UIMessage
import koala.SvgFile
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.zenBg
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/**
 * A section of a form under the heading [name], when given, a column of a [ColumnRow]. The cousin of the web's
 * `formSection`.
 */
@Composable
fun FlowRowScope.FormSection(name: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(1, modifier.rowColumn()) {
        name?.let { FormHeading(it) }
        content()
    }
}

/**
 * A card of a form's sections under the heading [name], when given, in a filigree, on the zen background, outlined, its
 * sections two units apart. The cousin of the web's `formCard`.
 */
@Composable
fun FormCard(name: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val theme = currentTheme
    Column(1, modifier.fillMaxWidth()) {
        name?.let {
            BasicText(
                it,
                Modifier.fillMaxWidth().filigree(),
                style = theme.text.heading3.copy(textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
        }
        Column(2, Modifier.fillMaxWidth().borderRadius(2, outline = Outline.Mid).zenBg().padding(2), content = content)
    }
}

/**
 * The heading of a form's section, [text] in capitals in the system's foreground, in a filigree. The cousin of the web's
 * `formHeading`.
 */
@Composable
fun FormHeading(text: String) {
    val theme = currentTheme
    BasicText(
        text.uppercase(),
        Modifier.fillMaxWidth().filigree(),
        style = theme.text.heading4.copy(textAlign = TextAlign.Center),
        color = { theme.colors.systemFg },
    )
}

/** The notes on a form's field, each a bulleted line, faint. The cousin of the web's `formBullets`. */
@Composable
fun FormBullets(vararg bullets: String) {
    val theme = currentTheme
    Column(0, Modifier.opacityHigh()) {
        bullets.forEach { bullet ->
            Row(1) {
                BasicText("•", style = theme.text.regular, color = { theme.colors.ink })
                BasicText(bullet, style = theme.text.regular, color = { theme.colors.ink })
            }
        }
    }
}

/**
 * The end of a form: the message of [messages], if any, beside the button reading [label] that calls [onClick] while
 * [isEnabled]. The cousin of the web's `formSubmit`.
 */
@Composable
fun FormSubmit(label: String, onClick: () -> Unit, messages: Tap<UIMessage?>, isEnabled: Boolean = true) {
    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { MessageBox(messages) }
        Button(label, onClick = onClick, isEnabled = isEnabled)
    }
}

/**
 * A box checked while [state] is `true`, before [label], toggling [state] when either is clicked. The cousin of koala's
 * `checkBox`.
 */
@Composable
fun CheckBox(state: MutableTap<Boolean>, label: String, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val isChecked by state.collectAsState()
    val shape = RoundedCornerShape(theme.unit / 2)
    Row(1, modifier.pointerClick { state.set(!isChecked) }, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(theme.unit * 3)
                .clip(shape)
                .background(if (isChecked) theme.colors.primaryButton else theme.colors.zenBg)
                .border(theme.line, if (isChecked) theme.colors.primaryButton else theme.colors.outlineMid, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (isChecked) Icon(SvgFile.Check.small, color = { Color.White }, size = theme.unit * 2)
        }
        BasicText(label, style = theme.text.regular, color = { theme.colors.ink })
    }
}
