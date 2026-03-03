package streetlight.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kampfire.model.GeoPoint
import pondui.ui.controls.Button
import pondui.ui.controls.LazyColumn
import pondui.ui.controls.Row
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import pondui.ui.controls.actionable
import pondui.ui.modifiers.pad
import pondui.ui.nav.LocalNav
import pondui.utils.current
import pondui.utils.rememberGeoLocator
import streetlight.app.StreetProfileRoute
import streetlight.app.utils.toRoute
import kotlin.time.Duration.Companion.seconds

@Composable
fun StreetProfileScreen(
    route: StreetProfileRoute,
    viewModel: StreetProfileModel = viewModel { StreetProfileModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()
    val nav = LocalNav.current
    val geoLocator = rememberGeoLocator()

//    val camera =
//        rememberCameraState(
//            firstPosition =
//                CameraPosition(target = Position(latitude = 45.521, longitude = -122.675), zoom = 13.0)
//        )

    Scaffold {
        Row(1) {
            TextField(
                text = state.newName,
                onChange = viewModel::setNewName,
                placeholder = "New Location Name",
                modifier = Modifier.weight(1f)
            )
            Button("Create", isEnabled = state.isValidNewItem, onClick = viewModel::createNewItem)
        }
//        Box(
//            contentAlignment = Alignment.Center
//        ) {
//            MaplibreMap(
//                baseStyle = BaseStyle.Uri("https://tiles.openfreemap.org/styles/liberty"),
//                cameraState = camera,
//                onMapClick = { pos, offset ->
//                    val features = camera.projection?.queryRenderedFeatures(offset)
//                    if (!features.isNullOrEmpty()) {
//                        println("Clicked on ${features[0].json()}")
//                        viewModel.setGeoPoint(GeoPoint(pos.longitude, pos.latitude))
//                        ClickResult.Consume
//                    } else {
//                        ClickResult.Pass
//                    }
//                },
//                modifier = Modifier.height(400.dp)
//            )
//            Icon(TablerIcons.Target)
//        }
        Row(1) {
            TextField(
                state.newLongitude,
                onChange = viewModel::setNewLongitude,
                placeholder = "Longitude",
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            TextField(
                state.newLatitude,
                onChange = viewModel::setNewLatitude,
                placeholder = "Latitude",
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Button("Locate", onClick = {
                geoLocator?.current(20.seconds) { location ->
                    location?.let {
                        viewModel.setGeoPoint(GeoPoint(location.longitude, location.latitude))
//                        camera.position = CameraPosition(
//                            target = Position(latitude = location.latitude, longitude = location.longitude),
//                            zoom = 20.0
//                        )
                    }
                }
            })
        }
        LazyColumn(1) {
            items(state.locations) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .actionable(it.locationId.toRoute())
                        .pad(1),
                ) {
                    Text(it.name)
                }
            }
        }

        LottieSpirit("monkey-dance")
    }
}