package streetlight.compose.view

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import kampfire.model.GeoPoint
import koala.model.MarkerId
import org.maplibre.compose.map.MapState
import org.maplibre.spatialk.geojson.Position
import streetlight.compose.utility.mixSrgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import coil3.compose.AsyncImage
import org.maplibre.compose.overlay.MapOverlayScope
import streetlight.compose.model.IconMarker
import streetlight.compose.model.EventMarker
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.Density
import kampfire.model.Url
import koala.Svg
import streetlight.compose.modifier.padding
import streetlight.model.data.EntityType
import streetlight.compose.model.MapMarker
import streetlight.compose.model.MarkerMap
import streetlight.compose.model.ThumbMarker
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/**
 * The markers of [markerMap] on the map of [mapState], each with its light beneath it; a click on one focuses it. A
 * marker shows once the zoom reaches its reveal zoom, or while it is focused, and its light alone shows before then.
 * The cousin of the web's `GeoLayerAdapter`.
 */
@Composable
fun MapOverlayScope.Markers(markerMap: MarkerMap, mapState: MapState) {
    val markers by markerMap.markersState.collectAsState()
    val focus by markerMap.focusState.collectAsState()
    val revealZooms = remember(markers) { revealZoomsOf(markers) }
    val revealed by remember(markers, revealZooms) {
        derivedStateOf {
            val zoom = mapState.cameraPosition.zoom
            val focusId = focus?.markerId
            markers.filter { revealZooms.getValue(it.markerId) <= zoom || it.markerId == focusId }
        }
    }

    Lights(markers, mapState)
    revealed.forEach { marker ->
        key(marker.markerId) {
            Marker(marker, marker.markerId == focus?.markerId, onClick = { markerMap.setFocus(marker) }) { MarkerBody(marker) }
        }
    }
}

/**
 * The light of each of [markers], drawn beneath them on the map of [mapState]: a dot in its type's color near white,
 * twinkling on its own phase, as the web's `LightLayer` draws them. The lights redraw each frame and never recompose.
 */
@Composable
private fun Lights(markers: List<MapMarker>, mapState: MapState) {
    val colors = currentTheme.colors
    val lights = remember(markers, colors) {
        markers.map { Light(it.geoPoint.toPosition(), colors.colorOf(it.type).mixSrgb(Color.White, LightTint), it.phase) }
    }
    val clock = remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) withFrameMillis { clock.longValue = it - start }
    }

    Spacer(
        Modifier.fillMaxSize().drawBehind {
            val seconds = clock.longValue / 1000f
            val radius = LightSize.toPx() / 2
            lights.forEach { light ->
                val screen = mapState.screenLocationFromPosition(light.position) ?: return@forEach
                val alpha = 0.4f - 0.2f * cos(Tau * (seconds + light.phase) / TwinkleSeconds)
                drawCircle(
                    Brush.radialGradient(
                        0f to light.color,
                        LightEdge to light.color,
                        1f to light.color.copy(alpha = 0f),
                        center = Offset(screen.x.toPx(), screen.y.toPx()),
                        radius = radius,
                    ),
                    radius = radius,
                    center = Offset(screen.x.toPx(), screen.y.toPx()),
                    alpha = alpha,
                )
            }
        }
    )
}

/** A marker's light: its [position], [color], and the [phase] of its twinkle in seconds. */
private class Light(val position: Position, val color: Color, val phase: Float)

/** The phase of this marker's twinkle in seconds, steady for its id, so each light twinkles on its own. */
private val MapMarker.phase get() = (markerId.hashCode() and 0xffff) / 65535f * TwinkleSeconds

/**
 * The zoom at which each of [markers] is revealed, by id. Stepping in from zoom 0 by half a zoom, each step reveals, in
 * list order, the markers farther than [RevealRadius] on screen from every marker already revealed; a marker never
 * clear of the others is revealed at no zoom. The web's `assignRevealZooms`.
 */
private fun revealZoomsOf(markers: List<MapMarker>): Map<MarkerId, Double> {
    val zooms = markers.associateTo(mutableMapOf()) { it.markerId to Double.POSITIVE_INFINITY }
    val points = markers.map { it.markerId to it.geoPoint.toWorldPoint() }
    val hidden = points.toMutableList()
    val revealed = mutableListOf<Offset>()
    var zoom = 0.0
    while (hidden.isNotEmpty() && zoom <= MaxRevealZoom) {
        // the radius in world units, the width of the world at zoom 0 being one tile
        val radius = RevealRadius / (TileSize * 2.0.pow(zoom))
        val radiusSq = (radius * radius).toFloat()
        hidden.removeAll { (id, point) ->
            if (revealed.any { (it - point).getDistanceSquared() <= radiusSq }) return@removeAll false
            zooms[id] = zoom
            revealed.add(point)
            true
        }
        zoom += RevealZoomStep
    }
    return zooms
}

/** This point in Web Mercator, the world spanning 0 to 1 on each axis. */
private fun GeoPoint.toWorldPoint(): Offset {
    val sinLat = sin(lat * PI / 180)
    val y = 0.5 - ln((1 + sinLat) / (1 - sinLat)) / (4 * PI)
    return Offset(((lng + 180) / 360).toFloat(), y.toFloat())
}

/**
 * The base of [marker] at its point: its [body] placed so the marker's body is centered there, grown by half while it
 * [isFocused], in the marker's scheme color. It fades in as it comes into view, as the web's marker body does on its
 * `@starting-style`. The cousin of the web's `toPointElement` and `MarkerStyle.Base`.
 */
@Composable
fun MapOverlayScope.Marker(marker: MapMarker, isFocused: Boolean, onClick: () -> Unit, body: @Composable () -> Unit) {
    val theme = currentTheme
    val half = with(LocalDensity.current) { (marker.bodySize / 2).roundToPx() }
    val scale by animateFloatAsState(if (isFocused) FocusScale else 1f, theme.magic.bounced())
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, theme.magic.eased()) }

    Box(
        Modifier
            .placedAt(marker.geoPoint.toPosition(), Alignment { size, _, _ -> IntOffset(-half, -size.height / 2) })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = appear.value
                transformOrigin = TransformOrigin(half.toFloat() / size.width, 0.5f)
            }
            .pointerClick(onClick),
    ) {
        CompositionLocalProvider(LocalSchemeColor provides theme.colors.colorOf(marker.type)) { body() }
    }
}

/** The body of [marker], chosen by its type, as each of the web's markers gives its `configureBody`. */
@Composable
fun MarkerBody(marker: MapMarker) = when (marker) {
    is ThumbMarker -> ThumbMarkerBody(marker)
    is IconMarker -> IconMarkerBody(marker)
    is EventMarker -> EventMarkerBody(marker)
}

/** The thumbnail of [marker], beside its label over its type and sublabel. The web's `configureThumbMarker`. */
@Composable
fun ThumbMarkerBody(marker: ThumbMarker) {
    MarkerBodyRow {
        MarkerThumb(marker.thumb, marker.bodySize)
        MarkerLabelColumn {
            MarkerLabel(marker.label)
            MarkerSublabel(marker.typeLabel, marker.sublabel)
        }
    }
}

/** The icon of [marker], beside its label. The web's `configureIconMarker`. */
@Composable
fun IconMarkerBody(marker: IconMarker) {
    MarkerBodyRow {
        MarkerIcon(marker.svg, marker.bodySize)
        MarkerLabel(marker.label)
    }
}

/**
 * The thumbnail of [marker], beside its label over a list of its type and sublabel, then its location's name in the
 * location's color, its type and location a step lighter than the web's. The cousin of the web's
 * `configureEventMarkerBody`.
 */
@Composable
fun EventMarkerBody(marker: EventMarker) {
    val theme = currentTheme
    MarkerBodyRow {
        MarkerThumb(marker.thumb, marker.bodySize)
        MarkerLabelColumn {
            MarkerLabel(marker.label)
            Column(Modifier.padding(start = 1)) {
                MarkerBullet { MarkerSublabel(marker.typeLabel, marker.sublabel, separator = " · ") }
                marker.locationName?.let {
                    val color = theme.colors.colorOf(EntityType.Location)
                    MarkerBullet {
                        BasicText(
                            it,
                            Modifier.widthIn(max = theme.unit * LabelWidthUnits),
                            style = markerTextStyle(theme.text.small).copy(fontWeight = FontWeight.Medium),
                            color = { color },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/** A marker's body: its parts in a row, a line apart. The web's `markerBodyRow`. */
@Composable
fun MarkerBodyRow(content: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(currentTheme.line), verticalAlignment = Alignment.CenterVertically, content = content)
}

/** The thumbnail at [thumb], a circle of [size] in a faint white ring. The web's `markerThumb`. */
@Composable
fun MarkerThumb(thumb: Url?, size: Dp) {
    AsyncImage(
        model = thumb,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(size).clip(CircleShape)
            .border(currentTheme.line, Color.White.copy(alpha = ThumbBorderAlpha), CircleShape),
    )
}

/** The icon [svg] in the scheme color, centered in a square of [size]. The web's `markerIcon`. */
@Composable
fun MarkerIcon(svg: Svg, size: Dp) {
    val color = LocalSchemeColor.current
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Icon(svg = svg, color = { color })
    }
}

/** The lines beside a marker's body, held to the label's width. The web's `markerLabelColumn`. */
@Composable
fun MarkerLabelColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.widthIn(max = currentTheme.unit * LabelWidthUnits), content = content)
}

/** A marker's [label], bold on one line. The web's `markerLabel`. */
@Composable
fun MarkerLabel(label: String) {
    val theme = currentTheme
    BasicText(
        label,
        style = markerTextStyle(theme.text.regular).copy(fontWeight = FontWeight.SemiBold),
        color = { theme.colors.ink },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * A marker's [typeLabel], medium in the scheme color, then its [sublabel] after a [separator], in small text. The web's
 * `markerSublabel`.
 */
@Composable
fun MarkerSublabel(typeLabel: String, sublabel: String?, separator: String = " • ") {
    val theme = currentTheme
    val color = LocalSchemeColor.current
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.Medium)) { append(typeLabel) }
        sublabel?.let {
            withStyle(SpanStyle(color = theme.colors.ink.copy(alpha = theme.opacity.half))) { append(separator) }
            withStyle(SpanStyle(color = theme.colors.muted)) { append(it) }
        }
    }
    BasicText(text, style = markerTextStyle(theme.text.small), maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** A line of a marker's list after its glyph, as the web's `bulletsOf` with its `›` glyph. */
@Composable
private fun MarkerBullet(content: @Composable () -> Unit) {
    val theme = currentTheme
    Row(horizontalArrangement = Arrangement.spacedBy(theme.unit / 2), verticalAlignment = Alignment.CenterVertically) {
        BasicText("›", style = markerTextStyle(theme.text.small), color = { theme.colors.muted })
        content()
    }
}

/** [style] in the marker's line height, edged in the paper color so it reads over the map, the web's `TextShadow`. */
@Composable
private fun markerTextStyle(style: TextStyle): TextStyle {
    val blur = with(LocalDensity.current) { blurRadiusOf(LabelShadowBlur) }
    return style.copy(lineHeight = MarkerLineHeight.em, shadow = Shadow(currentTheme.colors.paper, blurRadius = blur))
}

/** The blur radius in pixels that blurs as CSS does at [cssBlur]: CSS to a deviation of half its radius, Skia to 0.58 of it plus half a pixel. */
private fun Density.blurRadiusOf(cssBlur: Dp) = (cssBlur.toPx() / 2 - 0.5f) / 0.57735f

/** The size of a marker's body, as the web's thumb and icon markers. */
private val MapMarker.bodySize: Dp get() = when (this) {
    is ThumbMarker, is EventMarker -> 48.dp
    is IconMarker -> 32.dp
}

/** The line height of a marker's text, the web's `LineHeight115`. */
private const val MarkerLineHeight = 1.15f

/** The distance on screen a revealed marker keeps the others hidden within, the web's `clusterRadiusPx` of its markers. */
private const val RevealRadius = 160.0

/** The width of MapLibre's tile, the world's width at zoom 0. */
private const val TileSize = 512.0

/** The step of zoom between reveals, as the web's. */
private const val RevealZoomStep = 0.5

/** The deepest zoom a marker is revealed at, as the web's. */
private const val MaxRevealZoom = 22.0

/** The size of a light, the web's `LIGHT_SIZE_PX`. */
private val LightSize = 8.dp

/** The share of a light's color that is its type's, the rest white, as the web's `light`. */
private const val LightTint = 0.2f

/** The share of a light's radius that is solid before it softens to its edge, as the web's shader. */
private const val LightEdge = 0.8f

/** The length of a light's twinkle in seconds, as the web's. */
private const val TwinkleSeconds = 2.4f

/** A full turn in radians. */
private const val Tau = (2 * PI).toFloat()

/** The scale of a focused marker, as the web's. */
private const val FocusScale = 1.5f

/** The opacity of a thumbnail's white border, as the web's. */
private const val ThumbBorderAlpha = 0.6f

/** The blur of the paper edge behind a marker's text, the web's `0 0 4px` of `--text-shadow`. */
private val LabelShadowBlur = 4.dp

/** The widest a marker's label runs, in units, the web's `MaxWidth(28)`. */
private const val LabelWidthUnits = 28
