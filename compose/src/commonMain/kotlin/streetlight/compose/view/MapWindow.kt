package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.ExpandingAttributionButton
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import streetlight.compose.model.AppConfig
import streetlight.compose.model.MarkerMap
import kotlin.math.hypot
import kampfire.model.GeoPoint

/** The state of the app's one map, in the site's fjord style read from the server as the web reads it. */
@Composable
fun rememberAppMapState(): MapState {
    val config = koinInject<AppConfig>()
    return rememberMapState(
        baseStyle = BaseStyle.Uri(config.apiUrl + FjordStylePath),
        initialCameraPosition = cameraPositionOf(null),
    )
}

/** The camera looking over [center], or over Denver without one, at the web's pitch. */
fun cameraPositionOf(center: GeoPoint?) = CameraPosition(
    center = center?.toPosition() ?: Denver,
    zoom = if (center != null) PointZoom else 11.0,
    pitch = 45.0,
)

/**
 * The map of [state] with the markers of [markerMap], if any. The cousin of the web's `geoMapMount`, darkened toward its
 * edges, with MapLibre's scale bar, compass and attribution and without its logo.
 */
@Composable
fun MapWindow(state: MapState, markerMap: MarkerMap?, modifier: Modifier = Modifier) {
    // MapLibre Compose's default controls without its logo; the attribution of the map data stays
    MaplibreMap(
        modifier = modifier,
        state = state,
        overlay = {
            Spacer(Modifier.fillMaxSize().drawBehind { drawVignette() })
            markerMap?.let { Markers(it, state) }
            Box(Modifier.fillMaxSize().padding(MapOverlay.Spacing)) {
                DisappearingScaleBar(
                    metersPerDp = { state.viewport?.metersPerDpAtCenter ?: 0.0 },
                    zoom = { state.cameraPosition.zoom },
                    modifier = Modifier.align(Alignment.TopStart),
                )
                DisappearingCompassButton(Modifier.align(Alignment.TopEnd))
                ExpandingAttributionButton(Modifier.align(Alignment.BottomEnd))
            }
        },
    )
}

/**
 * Darkens the map toward its edges, as the web's map overlay does: clear at the center, black at 25% by 60% of the way
 * out and at 50% at the corners, the radius reaching the farthest corner as CSS's `circle` does.
 */
private fun DrawScope.drawVignette() {
    drawRect(
        Brush.radialGradient(
            0f to Color.Transparent,
            0.6f to Color.Black.copy(alpha = 0.25f),
            1f to Color.Black.copy(alpha = 0.5f),
            center = center,
            radius = hypot(size.width / 2, size.height / 2),
        )
    )
}

/** The path of the map style the server serves, as the web's `initGeoMap` loads it. */
private const val FjordStylePath = "/www/misc/fjord.json"

/** The web's starting point when it has none of its own. */
private val Denver = Position(longitude = -104.95, latitude = 39.75)

/** The zoom of a map looking over a single place. */
private const val PointZoom = 15.0

/** The position of this point on the map. */
fun GeoPoint.toPosition() = Position(longitude = lng, latitude = lat)
