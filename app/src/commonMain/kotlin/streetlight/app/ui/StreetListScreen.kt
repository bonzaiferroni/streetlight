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
import pondui.ui.modifiers.pad
import streetlight.app.utils.toRoute

@Composable
fun StreetListScreen(
    viewModel: StreetListModel = viewModel { StreetListModel() }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(state.newStreetName, onChange = viewModel::setNewStreetName)
            Button("Create", isEnabled = state.isValidNewItem, onClick = viewModel::createNewStreet)
        }
        LazyColumn(1) {
            items(state.communities) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .actionable(it.communityId.toRoute())
                        .pad(1),
                ) {
                    Text(it.name)
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ){
            LottieSpirit("boppin-fox.json", modifier = Modifier.width(100.dp))
        }
    }
}