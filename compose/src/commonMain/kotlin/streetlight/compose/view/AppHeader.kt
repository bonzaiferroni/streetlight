package streetlight.compose.view

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import streetlight.compose.modifier.FiligreeStyle
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.modifier.pointerClick
import streetlight.compose.model.currentPortal

/**
 * The header of a view: the [TextLogo] between the accent and primary rays, returning home when clicked. The cousin of the web's `appHeader`.
 */
@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val portal = currentPortal
    TextLogo(
        modifier.fillMaxWidth()
            .height(theme.unit * HeaderUnits)
            .padding(1)
            .filigree(FiligreeStyle.Ray)
            .pointerClick { portal.home() }
            .fillMaxHeight()
    )
}

/** The height of the header in units, the web's `Height(10)` on the home page. */
private const val HeaderUnits = 10
