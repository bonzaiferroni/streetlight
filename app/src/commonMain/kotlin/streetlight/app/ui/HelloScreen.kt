package streetlight.app.ui

import androidx.compose.runtime.Composable
import pondui.ui.controls.Button
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.nav.LocalNav
import streetlight.app.HelloRoute
import streetlight.app.StartRoute

@Composable
fun HelloScreen(route: HelloRoute) {
    val nav = LocalNav.current
    Scaffold {
        Text("Hello hello!")
        Button(onClick = {nav.go(StartRoute)}) {
            Text("Go to Start")
        }
    }
}