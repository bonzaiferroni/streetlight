package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import streetlight.app.LocationListRoute

@Composable
fun LocationListScreen(
    route: LocationListRoute,
    viewModel: LocationListModel = viewModel { LocationListModel(route) }
) {
    val state by viewModel.state.collectAsState()
}