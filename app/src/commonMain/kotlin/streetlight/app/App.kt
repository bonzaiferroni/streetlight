package streetlight.app

import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.FontResource
import pondui.io.ProvideUserContext
import pondui.ui.core.PondApp
import pondui.ui.nav.NavRoute
import pondui.ui.theme.ProvideTheme
import pondui.ui.theme.defaultTheme
import pondui.ui.theme.useFamily
import streetlight.app.generated.resources.FiraSans_Bold
import streetlight.app.generated.resources.FiraSans_Italic
import streetlight.app.generated.resources.FiraSans_Light
import streetlight.app.generated.resources.FiraSans_Regular
import streetlight.app.generated.resources.Res

@Composable
fun App(
    scale: Float = 1f,
    changeRoute: (NavRoute) -> Unit,
    exitApp: (() -> Unit)?,
) {
    ProvideTheme(
        theme = defaultTheme(
            scale = scale,
            baseFontSize = 15f,
            baseFont = useFamily(Res.font.FiraSans_Regular),
            italicFont = useFamily(Res.font.FiraSans_Italic),
            lightFont = useFamily(Res.font.FiraSans_Light, weight = FontWeight.Light),
            boldFont = useFamily(Res.font.FiraSans_Bold, weight = FontWeight.Bold),
        ),
    ) {
        ProvideUserContext {
            PondApp(
                config = appConfig,
                changeRoute = changeRoute,
                exitApp = exitApp
            )
        }
    }
}
