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
 * The header of a view: the [TextLogo] between the accent and primary rays, returning home when clicked. The rays line
 * up with the centers of the [HelmBar]'s icons and stop a unit short of them. The cousin of the web's `appHeader`.
 */
@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val portal = currentPortal
    TextLogo(
        modifier.fillMaxWidth()
            .height(theme.unit * HeaderUnits)
            .padding(horizontal = RayInsetUnits, vertical = 1)
            .filigree(FiligreeStyle.Ray)
            .pointerClick { portal.home() }
            .fillMaxHeight()
    )
}

/** The height of the header in units, its center on the centers of the helm bar's icons, a unit plus half an icon down. */
private const val HeaderUnits = 2 + HelmIconUnits

/** The inset of the rays from the sides in units: past the helm bar's padding and icon, and a unit clear of the icon. */
const val RayInsetUnits = 1 + HelmIconUnits + 1
