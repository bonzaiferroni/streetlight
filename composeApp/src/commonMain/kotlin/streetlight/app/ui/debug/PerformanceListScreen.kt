package streetlight.app.ui.debug

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.viewmodel.viewModelScope
import streetlight.app.Scenes
import streetlight.app.io.PerformanceDao
import streetlight.app.services.BusService
import streetlight.app.ui.debug.controls.DataList
import streetlight.app.ui.core.UiModel
import streetlight.app.ui.core.UiState
import streetlight.model.Performance

@Composable
fun PerformanceListScreen(navigator: Navigator?) {
    val viewModel = koinViewModel<PerformanceListModel>()
    val state by viewModel.state

    DataList(
        title = "Performances",
        items = state.performances,
        provideName = { it.name },
        floatingAction = {
            viewModel.onNewPerformance()
            Scenes.performanceEditor.go(navigator)
        },
        navigator = navigator,
        onEdit = { Scenes.performanceEditor.go(navigator, it.id) },
        onDelete = viewModel::deletePerformance,
    )
}

class PerformanceListModel(
    private val performanceDao: PerformanceDao,
    private val bus: BusService,
) : UiModel<PerformanceListState>(PerformanceListState()) {
    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val performances = performanceDao.getAll()
            sv = sv.copy(performances = performances)
        }
    }

    fun onNewPerformance() {
        bus.request<Performance> {
            refresh()
        }
    }

    fun deletePerformance(performance: Performance) {
        viewModelScope.launch(Dispatchers.IO) {
            performanceDao.delete(performance.id)
            refresh()
        }
    }
}

data class PerformanceListState(
    val performances: List<Performance> = emptyList(),
    val result: String = "",
) : UiState