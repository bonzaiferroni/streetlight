package streetlight.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.padding
import streetlight.compose.view.Button
import streetlight.compose.view.ButtonStyle

/** A button at the window's corner that returns to the previous page, shown while there is one. */
@Composable
fun BackButton() {
    val portal = currentPortal
    if (portal.backStack.size > 1) {
        Button("back", onClick = portal::back, Modifier.padding(1), style = ButtonStyle.Zen)
    }
}
