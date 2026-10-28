package streetlight.compose.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.zenBg

/**
 * An outlined container of a fixed height that scrolls the items to choose from built by [content], and whatever
 * follows them, so a list that grows or shrinks leaves the form around it in place. The cousin of koala's
 * `selectorContainer`.
 */
@Composable
fun SelectorContainer(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val theme = currentTheme
    Column(
        1,
        modifier.fillMaxWidth()
            .height(theme.unit * SelectorHeightUnits)
            .borderRadius(1, outline = Outline.Mid)
            .zenBg()
            .verticalScroll(rememberScrollState())
            .padding(1),
        content = content,
    )
}

/** The height of a selector in units, the web's `Height(32)`. */
private const val SelectorHeightUnits = 32
