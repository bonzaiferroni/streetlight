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
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.DisappearingCompassButton
import org.maplibre.compose.overlay.DisappearingScaleBar
import org.maplibre.compose.overlay.ExpandingAttributionButton
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.geojson.Position
import streetlight.compose.model.AppConfig
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.moonShadow
import streetlight.compose.modifier.outline
import kotlin.math.hypot

/**
 * The map in the site's fjord style, read from the server as the web reads it, looking over Denver at the web's
 * pitch. The cousin of the web's `geoMapMount`, framed as on the web's home, rounded two units, outlined, cast in the moon's shadow and darkened toward
 * its edges, with MapLibre's scale bar, compass and attribution and without its logo.
 */
@Composable
fun MapWindow(modifier: Modifier = Modifier) {
    val config = koinInject<AppConfig>()
    val state = rememberMapState(
        baseStyle = BaseStyle.Uri(config.apiUrl + FjordStylePath),
        initialCameraPosition = CameraPosition(center = Denver, zoom = 11.0, pitch = 45.0),
    )

    // MapLibre Compose's default controls without its logo; the attribution of the map data stays
    MaplibreMap(
        modifier = modifier.moonShadow(radius = 2).borderRadius(2).outline(radius = 2),
        state = state,
        overlay = {
            Spacer(Modifier.fillMaxSize().drawBehind { drawVignette() })
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
