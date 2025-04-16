package streetlight.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import pondui.ui.core.Blapp
import pondui.ui.nav.NavRoute
import pondui.ui.theme.ProvideSkyColors
import pondui.ui.theme.ProvideTheme
import pondui.ui.theme.defaultTheme
import pondui.ui.theme.useFamily
import streetlight.app.generated.resources.Inter_18pt_Light
import streetlight.app.generated.resources.Inter_18pt_Regular
import streetlight.app.generated.resources.Inter_24pt_Light
import streetlight.app.generated.resources.Inter_28pt_Light
import streetlight.app.generated.resources.Res

@Composable
@Preview
fun App(
    initialRoute: NavRoute,
    changeRoute: (NavRoute) -> Unit,
    exitApp: (() -> Unit)?,
) {
    ProvideTheme(
        theme = defaultTheme(
            baseFont = useFamily(Res.font.Inter_18pt_Regular),
            h1Font = useFamily(Res.font.Inter_28pt_Light, FontWeight.Light),
            h2Font = useFamily(Res.font.Inter_24pt_Light, FontWeight.Light),
            h4Font = useFamily(Res.font.Inter_18pt_Light, FontWeight.Light),
        )
    ) {
        ProvideSkyColors {
            Blapp(
                initialRoute = initialRoute,
                changeRoute = changeRoute,
                config = appConfig,
                exitApp = exitApp
            )
        }
    }
}