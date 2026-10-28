package streetlight.compose.view

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import koala.SvgFile
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/**
 * The sections of a form side by side, each at least [FormSectionMinUnits] wide and sharing the width, wrapping onto
 * more rows where they do not fit. The cousin of the web's `formRow`.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormRow(modifier: Modifier = Modifier, content: @Composable FlowRowScope.() -> Unit) {
    val theme = currentTheme
    FlowRow(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.unit * 2),
        verticalArrangement = Arrangement.spacedBy(theme.unit * 2),
        content = content,
    )
}

/** A section of a form under the heading [name], when given, filling its share of a [FormRow]. */
@Composable
fun FlowRowScope.FormSection(name: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(1, modifier.widthIn(min = currentTheme.unit * FormSectionMinUnits).weight(1f)) {
        name?.let { FormHeading(it) }
        content()
    }
}

/** The heading of a form's section, [text] in capitals in a filigree. The cousin of the web's `formHeading`. */
@Composable
fun FormHeading(text: String) {
    val theme = currentTheme
    BasicText(
        text.uppercase(),
        Modifier.fillMaxWidth().filigree(),
        style = theme.text.heading4.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
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
 * The end of a form: the [message] to the star, if any, beside the button reading [label] that calls [onClick] while
 * [isEnabled]. The cousin of the web's `formSubmit`.
 */
@Composable
fun FormSubmit(label: String, onClick: () -> Unit, message: String?, isEnabled: Boolean = true) {
    val theme = currentTheme
    Row(1, Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BasicText(message.orEmpty(), Modifier.weight(1f), style = theme.text.regular, color = { theme.colors.ink })
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

/** The narrowest a form's section runs in units, before the sections of a row wrap. */
const val FormSectionMinUnits = 32
