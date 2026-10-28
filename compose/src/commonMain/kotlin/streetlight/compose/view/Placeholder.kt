package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.cardBg
import streetlight.compose.modifier.padding

/** The place of [subject], which waits for its own work, on a card that says it is coming. */
@Composable
fun Placeholder(subject: String) {
    val theme = currentTheme
    BasicText(
        "$subject, coming soon",
        Modifier.fillMaxWidth().borderRadius(2, outline = Outline.Mid).cardBg().padding(2),
        style = theme.text.regular.copy(textAlign = TextAlign.Center),
        color = { theme.colors.ink },
    )
}
