package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import streetlight.app.EventFeedRoute

@Composable
fun EventFeedScreen(
    route: EventFeedRoute,
    viewModel: EventFeedModel = viewModel { EventFeedModel(route) }
) {
    val state by viewModel.state.collectAsState()
}