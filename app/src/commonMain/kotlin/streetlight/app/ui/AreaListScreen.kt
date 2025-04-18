package streetlight.app.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import streetlight.app.AppRoute
import streetlight.app.AreaListRoute

@Composable
fun AreaListScreen(
    route: AreaListRoute,
    viewModel: AreaListModel = viewModel { AreaListModel(route) }
) {
    val state by viewModel.state.collectAsState()
}