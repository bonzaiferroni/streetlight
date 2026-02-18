package streetlight.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import pondui.ui.controls.AppWindow
import pondui.ui.controls.LocalAppWindow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            val cfg = LocalConfiguration.current
            val scale = 1.25f
            val appWindow = remember(cfg) {
                AppWindow(cfg.screenWidthDp, cfg.screenHeightDp, scale)
            }
            CompositionLocalProvider(LocalAppWindow provides appWindow) {
                Box(
                    Modifier
                        .background(Color.Black)
                        .fillMaxSize()
                        .safeDrawingPadding() // keeps clear of status + nav bars
                ) {
                    App(scale, { }, { })
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(
        changeRoute = { },
        exitApp = { }
    )
}