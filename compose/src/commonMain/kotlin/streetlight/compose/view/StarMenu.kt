package streetlight.compose.view

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import streetlight.compose.modifier.padding

/**
 * The star's menu, as it shows before a star signs in: their name beside their [badge], and the button to sign in,
 * which does nothing until the app has sign-in. The cousin of the web's star helm.
 */
@Composable
fun StarMenu(badge: @Composable () -> Unit) {
    val theme = currentTheme
    Column(1, Modifier.padding(1), horizontalAlignment = Alignment.End) {
        Row(1, verticalAlignment = Alignment.CenterVertically) {
            BasicText("Someone", style = theme.text.heading3, color = { theme.colors.ink })
            badge()
        }
        Button("sign in", onClick = {})
    }
}
