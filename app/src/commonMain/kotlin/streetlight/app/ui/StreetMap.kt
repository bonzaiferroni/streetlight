package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import streetlight.model.data.Location

@Composable
fun StreetMap(locations: ImmutableList<Location>) {
//    val positionList = locations.map { Point(Position(it.geoPoint.longitude, it.geoPoint.latitude)) }
//    val cameraState =
//        rememberCameraState(
//            firstPosition = CameraPosition(
//                target = Position(
//                    latitude = 39.749361,
//                    longitude = -104.997667
//                ), zoom = 7.0
//            ),
//        )

    Box(
        modifier = Modifier.fillMaxWidth()
            .height(400.dp)
    ) {
//        if (positionList.isNotEmpty()) {
//            MaplibreMap(
//                styleUri = "https://tiles.openfreemap.org/styles/liberty",
//                cameraState = cameraState,
//                logger = null,
//            ) {
//                val customPoints = rememberGeoJsonSource("locations", data = GeometryCollection(positionList))
//                CircleLayer(id = "locations", source = customPoints)
//            }
//        }
    }
}

//            KcefProvider(
//                loading = { BasicText("Performing first time setup ...") },
//            ) {
//                MaplibreContextProvider {
//                }
//            }