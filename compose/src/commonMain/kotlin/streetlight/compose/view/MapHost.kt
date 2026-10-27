package streetlight.compose.view

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.toSize
import kampfire.model.GeoPoint
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.compose.runtime.snapshotFlow
import kampfire.model.GeoRect
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import org.maplibre.compose.camera.CameraUpdate
import org.maplibre.spatialk.geojson.Position
import streetlight.compose.model.GeoCameraState
import streetlight.compose.model.GeoMap
import streetlight.compose.model.MarkerMap
import streetlight.compose.utility.collectAsState

/**
 * A place on a page where the app's one map shows: its [bounds] in the root, the [center] the map looks over there, the
 * [radius] of its corners in units, and the page's [markerMap], if any.
 */
class MapMountState(center: GeoPoint?, radius: Float, markerMap: MarkerMap?) {
    var bounds by mutableStateOf(Rect.Zero)
    var center by mutableStateOf(center)
    var radius by mutableStateOf(radius)
    var markerMap by mutableStateOf(markerMap)
}

/** The mounts of the pages that show the map, the latest the one it shows in. */
class MapHostState {
    val mounts = mutableStateListOf<MapMountState>()
    val active get() = mounts.lastOrNull()
}

/** The [MapHostState] of the app's one map, provided by [MapHost]. */
val LocalMapHost = staticCompositionLocalOf<MapHostState> { error("No MapHost provided") }

/**
 * Holds the app's one map above [content] for as long as the app runs, so a page that shows the map never starts its
 * renderer. The map shows in the mount of the latest page that holds one, in its rounded frame and outline. When the
 * mount it shows in and the new one are both in the window, its frame grows or shrinks from one to the other as the
 * pages fade beneath it, gliding to the new mount's center, with the new mount's markers from the start. Otherwise
 * it fades out, moves to the mount, shows the mount's markers through [GeoMap] and looks over its center, then fades in,
 * as a page does. Without a mount it stays, unseen. It applies the camera's pans and reports where it looks, the cousin of the
 * web's `wireGeoMap`.
 */
@Composable
fun MapHost(content: @Composable () -> Unit) {
    val host = remember { MapHostState() }
    val magic = currentTheme.magic
    val state = rememberAppMapState()
    val geoMap = koinInject<GeoMap>()
    val markerMap by geoMap.markerMapState.collectAsState()
    val active = host.active
    var shown by remember { mutableStateOf<MapMountState?>(null) }
    // the mounts report their bounds in the root, and the map is placed in the host, which may sit below a bar
    var window by remember { mutableStateOf(Rect.Zero) }
    val alpha = remember { Animatable(0f) }
    var morph by remember { mutableStateOf<MapMorph?>(null) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(active) {
        // a morph cut short by a newer mount ends where it stands
        morph = null
        val from = shown
        if (active != null && from != null && alpha.value == 1f && from.bounds.isWithin(window)) {
            // a new mount reports its bounds once its page is laid out
            val to = snapshotFlow { active.bounds }.first { it != Rect.Zero }
            if (to.isWithin(window)) {
                morph = MapMorph(from.bounds, from.radius, active)
                shown = active
                geoMap.show(active.markerMap)
                progress.snapTo(0f)
                launch { state.animateCamera(cameraUpdateOf(active.center)) }
                progress.animateTo(1f, tween(magic.interval * 2, easing = magic.easing))
                morph = null
                return@LaunchedEffect
            }
        }
        alpha.animateTo(0f, magic.eased())
        shown = active
        geoMap.show(active?.markerMap)
        active?.let { state.setCameraPosition(cameraPositionOf(it.center)) }
        if (active != null) alpha.animateTo(1f, magic.eased())
    }

    LaunchedEffect(state) {
        geoMap.camera.panFlow.collectLatest { pan ->
            state.animateCamera(CameraUpdate(center = pan.point.toPosition(), zoom = pan.zoom))
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.viewport to state.isCameraMoving }.collect { (viewport, isMoving) ->
            viewport ?: return@collect
            val camera = viewport.cameraPosition
            val bounds = viewport.visibleBounds
            geoMap.camera.report(
                GeoCameraState(
                    center = camera.center.toGeoPoint(),
                    bounds = GeoRect(bounds.southwest.toGeoPoint(), bounds.northeast.toGeoPoint()),
                    zoom = camera.zoom,
                    isMoving = isMoving,
                )
            )
        }
    }

    CompositionLocalProvider(LocalMapHost provides host) {
        Box(Modifier.onGloballyPositioned { window = Rect(it.positionInRoot(), it.size.toSize()) }) {
            content()
            val mount = shown
            val radius = morph?.let { lerp(it.fromRadius, it.to.radius, progress.value) } ?: mount?.radius ?: 0f
            MapWindow(
                state,
                markerMap,
                Modifier
                    .layout { measurable, constraints ->
                        // the map takes its mount's size and place, or a sliver while it has none
                        val bounds = morph?.let { lerp(it.from, it.to.bounds, progress.value) } ?: mount?.bounds ?: Rect(0f, 0f, 1f, 1f)
                        val width = bounds.width.toInt().coerceAtLeast(1)
                        val height = bounds.height.toInt().coerceAtLeast(1)
                        val placeable = measurable.measure(Constraints.fixed(width, height))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place((bounds.left - window.left).toInt(), (bounds.top - window.top).toInt())
                        }
                    }
                    .graphicsLayer { this.alpha = alpha.value }
                    .borderRadius(radius, outline = Outline.Mid),
            )
        }
    }
}

/**
 * A mount where the app's one map shows on this page, looking over [center], or Denver without one, with corners of
 * [radius] units, showing the markers of [markerMap]. The mount holds the map's place in the page's layout; the [MapHost]
 * draws the map into it. It leaves the host as its page starts to leave, so the map fades out with the page.
 */
@Composable
fun MapMount(modifier: Modifier = Modifier, center: GeoPoint? = null, radius: Number = 2, markerMap: MarkerMap? = null) {
    val host = LocalMapHost.current
    val mount = remember { MapMountState(center, radius.toFloat(), markerMap) }
    mount.center = center
    mount.radius = radius.toFloat()
    mount.markerMap = markerMap
    val isLeaving = LocalNavAnimatedContentScope.current.transition.targetState == EnterExitState.PostExit
    DisposableEffect(mount, isLeaving) {
        if (!isLeaving) host.mounts.add(mount)
        onDispose { host.mounts.remove(mount) }
    }
    Spacer(modifier.onGloballyPositioned { mount.bounds = Rect(it.positionInRoot(), it.size.toSize()) })
}

/** A change of the map's frame from the bounds [from] and corners [fromRadius] of the mount it left to the mount [to]. */
private class MapMorph(val from: Rect, val fromRadius: Float, val to: MapMountState)

/** Whether these bounds have area and lie within [window], as a mount the eye can follow does. */
private fun Rect.isWithin(window: Rect) = width > 0f && height > 0f && window.contains(topLeft) && window.contains(bottomRight - Offset(1f, 1f))

/** The camera update that looks over [center], as [cameraPositionOf] does. */
private fun cameraUpdateOf(center: GeoPoint?) = cameraPositionOf(center).let { CameraUpdate(center = it.center, zoom = it.zoom) }

/** The point of this position. */
private fun Position.toGeoPoint() = GeoPoint(lng = longitude, lat = latitude)
