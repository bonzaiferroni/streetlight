package streetlight.compose.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.ExpandingAttributionButton
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import streetlight.compose.model.AppConfig

/**
 * The map in the site's fjord style, read from the server as the web reads it, looking over Denver at the web's
 * pitch. The cousin of the web's `geoMapMount`, framed with a two-unit radius and an `outlineLow` border, with
 * MapLibre's scale bar, compass and attribution and without its logo.
 */
@Composable
fun MapWindow(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val config = koinInject<AppConfig>()
    val shape = RoundedCornerShape(theme.unit * 2)
    val state = rememberMapState(
        baseStyle = BaseStyle.Uri(config.apiUrl + FjordStylePath),
        initialCameraPosition = CameraPosition(center = Denver, zoom = 11.0, pitch = 45.0),
    )

    // MapLibre Compose's default controls without its logo; the attribution of the map data stays
    MaplibreMap(
        modifier = modifier.clip(shape).border(BorderWidth, theme.colors.outlineLow, shape),
        state = state,
        overlay = {
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

/** The path of the map style the server serves, as the web's `initGeoMap` loads it. */
private const val FjordStylePath = "/www/misc/fjord.json"

/** The web's starting point when it has none of its own. */
private val Denver = Position(longitude = -104.95, latitude = 39.75)

private val BorderWidth = 2.dp
