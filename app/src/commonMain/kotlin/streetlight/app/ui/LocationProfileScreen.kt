package streetlight.app.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.io.LocalUserContext
import pondui.io.collectState
import pondui.ui.controls.Button
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import streetlight.app.LocationProfileRoute
import streetlight.model.data.Location
import kotlin.reflect.KProperty1

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationProfileScreen(
    route: LocationProfileRoute,
    viewModel: LocationProfileModel = viewModel { LocationProfileModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val userState by LocalUserContext.collectState()

    val modLocation = state.modLocation ?: return

    Scaffold {
        FlowRow(
            gap = 1,
            maxItemsInEachRow = 2,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val readOnly = userState.user?.userId != modLocation.userId
            ModText(
                item = modLocation,
                property = remember { Location::name },
                readOnly = readOnly,
                modifyItem = { viewModel.modifyItem(modLocation.copy(name = it ?: "")) },
                modifier = Modifier.weight(1f)
            )
            ModText(
                item = modLocation,
                property = remember { Location::address },
                readOnly = readOnly,
                modifyItem = { viewModel.modifyItem(modLocation.copy(address = it)) },
                modifier = Modifier.weight(1f)
            )
            ModText(
                item = modLocation,
                property = remember { Location::description },
                readOnly = readOnly,
                modifyItem = { viewModel.modifyItem(modLocation.copy(description = it)) },
                modifier = Modifier.weight(1f)
            )
            Button(
                text = "Update", isEnabled = state.isValidUpdate,
                onClick = viewModel::updateItem, modifier = Modifier.fillMaxRowHeight()
            )
        }
    }
}

@Composable
fun <T> ModText(
    item: T,
    property: KProperty1<T, String?>,
    readOnly: Boolean,
    placeholder: String = property.name,
    modifyItem: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (readOnly) {
        val value = property.get(item)
        if (value != null) {
            Text(value, modifier = modifier)
        }
    } else {
        TextField(property.get(item) ?: "", onValueChange = { modifyItem(it.takeIf { it.isNotBlank() })
        }, placeholder = placeholder, modifier = modifier)
    }
}

//@Composable
//fun <T> ModText(
//    item: T,
//    property: KProperty1<T, String>,
//    readOnly: Boolean,
//    placeholder: String = property.name,
//    modifyItem: (String) -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    if (readOnly) {
//        Text(property.get(item), modifier = modifier)
//    } else {
//        TextField(property.get(item), modifyItem, placeholder = placeholder, modifier = modifier)
//    }
//}