package streetlight.app

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.compose.rememberNavController
import kotlinx.browser.document
import kotlinx.browser.window
import pondui.ui.controls.Text

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val navController = rememberNavController()
        App(
            initialRoute = StartRoute,
            navController = navController,
            changeRoute = { },
            exitApp = null,
        )
//        LaunchedEffect(Unit) {
//            window.bindToNavigation(navController)
//        }
    }
}