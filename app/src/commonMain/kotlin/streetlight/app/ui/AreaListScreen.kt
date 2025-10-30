package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.*
import pondui.ui.nav.LocalNav
import streetlight.app.AreaProfileRoute
import streetlight.app.utils.toRoute

@Composable
fun AreaListScreen(
    viewModel: AreaListModel = viewModel { AreaListModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(state.newAreaName, onChange = viewModel::setNewAreaName)
            Button("Create", isEnabled = state.isValidNewItem, onClick = viewModel::createNewArea)
        }
        LazyColumn(1) {
            items(state.areas) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .actionable(it.areaId.toRoute()),
                ) {
                    Text(it.name)
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            Spirit("boppin-fox.json", modifier = Modifier.width(400.dp))
        }
    }
}