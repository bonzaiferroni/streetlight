package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject
import streetlight.compose.model.ThemeHost
import streetlight.model.data.PageTheme

/** Shows [theme] as the page's theme while the view that calls it is shown, the default theme when it is `null`. */
@Composable
fun PageThemeEffect(theme: PageTheme?) {
    val themeHost = koinInject<ThemeHost>()
    LaunchedEffect(theme) { themeHost.setTheme(theme) }
}
