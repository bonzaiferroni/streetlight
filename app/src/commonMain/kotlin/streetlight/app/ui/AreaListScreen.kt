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
import pondui.ui.nav.Scaffold
import pondui.ui.theme.Pond
import streetlight.app.AreaListRoute
import streetlight.app.AreaProfileRoute

@Composable
fun AreaListScreen(
    route: AreaListRoute,
    viewModel: AreaListModel = viewModel { AreaListModel(route) }
) {
    val state by viewModel.state.collectAsState()
    val nav = LocalNav.current

    Scaffold {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(state.newAreaName, onTextChange = viewModel::setNewAreaName)
            Button("Create", isEnabled = state.isValidNewItem, onClick = viewModel::createNewArea)
        }
        LazyColumn(
            verticalArrangement = Pond.ruler.columnTight,
        ) {
            items(state.areas) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(it.name)
                    Button("➡") { nav.go(AreaProfileRoute(it.id)) }
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