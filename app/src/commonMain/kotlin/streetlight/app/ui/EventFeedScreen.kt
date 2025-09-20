package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Scaffold
import streetlight.app.EventFeedRoute

@Composable
fun EventFeedScreen(
    route: EventFeedRoute,
    viewModel: EventFeedModel = viewModel { EventFeedModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()
    Scaffold {

    }
}