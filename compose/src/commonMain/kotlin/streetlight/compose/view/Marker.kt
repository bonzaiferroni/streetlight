package streetlight.compose.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import coil3.compose.AsyncImage
import org.maplibre.compose.overlay.MapOverlayScope
import streetlight.compose.model.IconMarker
import streetlight.compose.model.MapMarker
import streetlight.compose.model.MarkerMap
import streetlight.compose.model.ThumbMarker
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/** The markers of [markerMap] on the map, each placed at its point; a click on one focuses it. */
@Composable
fun MapOverlayScope.Markers(markerMap: MarkerMap) {
    val markers by markerMap.markersState.collectAsState()
    val focus by markerMap.focusState.collectAsState()
    markers.forEach { marker ->
        key(marker.markerId) {
            Marker(marker, marker.markerId == focus?.markerId, onClick = { markerMap.setFocus(marker) })
        }
    }
}

/**
 * The [marker] at its point: its body centered there, its label beside it, grown by half while it [isFocused]. The
 * cousin of the web's `configureThumbMarker` and `configureIconMarker`.
 */
@Composable
fun MapOverlayScope.Marker(marker: MapMarker, isFocused: Boolean, onClick: () -> Unit) {
    val theme = currentTheme
    val color = theme.colors.colorOf(marker.type)
    val bodySize = marker.bodySize
    val half = with(LocalDensity.current) { (bodySize / 2).roundToPx() }
    val scale by animateFloatAsState(if (isFocused) FocusScale else 1f, theme.magic.bounced())
    val labelStyle = theme.text.regular.copy(
        fontWeight = FontWeight.Bold,
        shadow = Shadow(theme.colors.paper, blurRadius = LabelShadowBlur),
    )

    Row(
        Modifier
            .placedAt(marker.geoPoint.toPosition(), Alignment { size, _, _ -> IntOffset(-half, -size.height / 2) })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(half.toFloat() / size.width, 0.5f)
            }
            .pointerClick(onClick),
        horizontalArrangement = Arrangement.spacedBy(theme.line),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (marker) {
            is ThumbMarker -> AsyncImage(
                model = marker.thumb,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(bodySize).clip(CircleShape)
                    .border(theme.line, Color.White.copy(alpha = ThumbBorderAlpha), CircleShape),
            )
            is IconMarker -> Box(Modifier.size(bodySize), contentAlignment = Alignment.Center) {
                Icon(svg = marker.svg, color = { color })
            }
        }
        Column(Modifier.widthIn(max = theme.unit * LabelWidthUnits)) {
            BasicText(marker.label, style = labelStyle, color = { theme.colors.ink }, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (marker is ThumbMarker) {
                val sublabel = buildAnnotatedString {
                    withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(marker.typeLabel) }
                    marker.sublabel?.let {
                        withStyle(SpanStyle(color = theme.colors.ink.copy(alpha = theme.opacity.half))) { append(" • ") }
                        withStyle(SpanStyle(color = theme.colors.muted)) { append(it) }
                    }
                }
                BasicText(sublabel, style = theme.text.small.copy(shadow = labelStyle.shadow), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** The size of a marker's body, as the web's thumb and icon markers. */
private val MapMarker.bodySize: Dp get() = when (this) {
    is ThumbMarker -> 48.dp
    is IconMarker -> 32.dp
}

/** The scale of a focused marker, as the web's. */
private const val FocusScale = 1.5f

/** The opacity of a thumbnail's white border, as the web's. */
private const val ThumbBorderAlpha = 0.6f

/** The blur of the paper glow behind a marker's label, the web's `--map-text-shadow`. */
private const val LabelShadowBlur = 12f

/** The widest a marker's label runs, in units, the web's `MaxWidth(28)`. */
private const val LabelWidthUnits = 28
