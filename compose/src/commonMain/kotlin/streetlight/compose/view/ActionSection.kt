package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.modifier.zenBg

/**
 * A section of [content] under its [heading] of [label], with its [actions] at the end of the heading's row, over a
 * rule. The cousin of the web's `actionSection`.
 */
@Composable
fun ActionSection(
    label: String,
    modifier: Modifier = Modifier,
    heading: @Composable RowScope.(String) -> Unit = { SectionHeading(it) },
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = currentTheme
    Column(1, modifier.fillMaxWidth()) {
        Row(
            1,
            Modifier.fillMaxWidth().drawBehind {
                val y = size.height - theme.line.toPx() / 2
                drawLine(theme.colors.outlineMid, Offset(0f, y), Offset(size.width, y), theme.line.toPx())
            }.padding(bottom = 1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            heading(label)
            androidx.compose.foundation.layout.Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(theme.unit, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
        content()
    }
}

/**
 * An [ActionSection] on an outlined card, its heading as a [FormSection]'s, as a form shows a list the star adds to
 * and edits. The cousin of the web's `actionSectionCard`.
 */
@Composable
fun ActionSectionCard(
    label: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val theme = currentTheme
    ActionSection(
        label,
        modifier.borderRadius(2, outline = Outline.Mid).zenBg().padding(2),
        heading = {
            BasicText(it.uppercase(), style = theme.text.heading4, color = { theme.colors.systemFg })
        },
        actions = actions,
        content = content,
    )
}

/** A row of [content], taking the room there is, beside its [actions]. The cousin of the web's `actionRow`. */
@Composable
fun ActionRow(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(1, modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(1, Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) { content() }
        actions()
    }
}

@Composable
fun ActionButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val theme = currentTheme
    BasicText(
        label.uppercase(),
        modifier.pointerClick(onClick).opacityHigh().padding(horizontal = 1),
        style = theme.text.small.copy(fontWeight = FontWeight.Bold),
        color = { theme.colors.ink },
    )
}

/** The heading of an [ActionSection], the web's `defaultSectionHeading`. */
@Composable
private fun SectionHeading(label: String) {
    val theme = currentTheme
    BasicText(
        label,
        Modifier.opacityHigh(),
        style = theme.text.heading3.copy(fontWeight = FontWeight.Bold),
        color = { theme.colors.ink },
    )
}
