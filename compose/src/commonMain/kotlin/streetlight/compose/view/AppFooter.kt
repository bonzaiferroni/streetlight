package streetlight.compose.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import koala.SvgFile
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.opacityHigh
import streetlight.compose.modifier.padding

/**
 * The footer of a view: a line picked from [prayers], the site's links, and a link to the source at [sourcePath] on
 * GitHub when given. The cousin of the web's `appFooter`, without its lottie, its site links unlinked until the app has
 * their routes.
 */
@Composable
fun AppFooter(sourcePath: String? = null, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val uriHandler = LocalUriHandler.current
    val prayer = remember { prayers.random() }
    Column(
        modifier.fillMaxWidth().padding(bottom = FooterBottomUnits),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BasicText(
            text = prayer,
            modifier = Modifier.opacityHigh(),
            style = theme.text.regular.copy(fontStyle = FontStyle.Italic),
            color = { theme.colors.ink },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(theme.unit)) {
            SiteLinks.forEachIndexed { index, label ->
                if (index > 0) BasicText("•", style = theme.text.regular, color = { theme.colors.ink })
                BasicText(label, style = theme.text.regular, color = { theme.colors.ink })
            }
        }
        sourcePath?.let { path ->
            Column(
                Modifier.padding(top = SourceTopUnits).clickable { uriHandler.openUri(sourceUrlOf(path)) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(SvgFile.Github.large, color = { theme.colors.ink }, modifier = Modifier.filigree(), size = theme.unit * 4)
                BasicText("source code for this content", style = theme.text.regular, color = { theme.colors.ink })
            }
        }
    }
}

private fun sourceUrlOf(path: String) = "https://github.com/bonzaiferroni/streetlight/blob/main/$path"

/** The footer's site links, as the web's. */
private val SiteLinks = listOf("about us", "your privacy", "feedback", "report a bug")

/** The lines the footer picks from at random, as the web's. */
private val prayers = listOf(
    "May we build a world of faithful giants.",
)

/** The space below the footer in units, the web's `MarginBottom(16)`. */
private const val FooterBottomUnits = 16

/** The space above the source link in units, the web's `MarginTop(4)`. */
private const val SourceTopUnits = 4
