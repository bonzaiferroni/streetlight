package streetlight.compose.view

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding

/**
 * The body of a page of settings or tools: [title] in a filigree, when given, over [content], in a column scrolled by
 * [scrollState] under the top bar, padded a unit all around as an item is, in the default theme. The cousin of the
 * web's `configBody`.
 */
@Composable
fun ConfigBody(title: String?, scrollState: ScrollState, content: @Composable ColumnScope.() -> Unit) {
    val theme = currentTheme
    PageThemeEffect(null)

    Column(2, Modifier.fillMaxSize().verticalScroll(scrollState).padding(currentPageInsets).padding(1)) {
        title?.let {
            BasicText(
                it,
                Modifier.fillMaxWidth().filigree(),
                style = theme.text.heading2.copy(textAlign = TextAlign.Center),
                color = { theme.colors.ink },
            )
        }
        content()
    }
}
