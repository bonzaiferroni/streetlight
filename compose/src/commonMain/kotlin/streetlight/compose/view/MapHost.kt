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

/**
 * A place on a page where the app's one map shows: its [bounds] in the root, the [center] the map looks over there, and
 * the [radius] of its corners in units.
 */
class MapSlotState(center: GeoPoint?, radius: Float) {
    var bounds by mutableStateOf(Rect.Zero)
    var center by mutableStateOf(center)
    var radius by mutableStateOf(radius)
}

/** The slots of the pages that show the map, the latest the one it shows in. */
class MapHostState {
    val slots = mutableStateListOf<MapSlotState>()
    val active get() = slots.lastOrNull()
}

/** The [MapHostState] of the app's one map, provided by [MapHost]. */
val LocalMapHost = staticCompositionLocalOf<MapHostState> { error("No MapHost provided") }

/**
 * Holds the app's one map above [content] for as long as the app runs, so a page that shows the map never starts its
 * renderer. The map shows in the slot of the latest page that holds one, in its rounded frame and outline: it fades
 * out, moves to the slot and looks over its center, then fades in, as a page does. Without a slot it stays, unseen.
 */
@Composable
fun MapHost(content: @Composable () -> Unit) {
    val host = remember { MapHostState() }
    val magic = currentTheme.magic
    val state = rememberAppMapState()
    val active = host.active
    var shown by remember { mutableStateOf<MapSlotState?>(null) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(active) {
        alpha.animateTo(0f, magic.eased())
        shown = active
        active?.let { state.setCameraPosition(cameraPositionOf(it.center)) }
        if (active != null) alpha.animateTo(1f, magic.eased())
    }

    CompositionLocalProvider(LocalMapHost provides host) {
        Box {
            content()
            val slot = shown
            MapWindow(
                state,
                Modifier
                    .layout { measurable, constraints ->
                        // the map takes its slot's size and place, or a sliver while it has none
                        val bounds = slot?.bounds ?: Rect(0f, 0f, 1f, 1f)
                        val width = bounds.width.toInt().coerceAtLeast(1)
                        val height = bounds.height.toInt().coerceAtLeast(1)
                        val placeable = measurable.measure(Constraints.fixed(width, height))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(bounds.left.toInt(), bounds.top.toInt())
                        }
                    }
                    .graphicsLayer { this.alpha = alpha.value }
                    .borderRadius(slot?.radius ?: 0f, outline = Outline.Mid),
            )
        }
    }
}

/**
 * A slot where the app's one map shows on this page, looking over [center], or Denver without one, with corners of
 * [radius] units. The slot holds the map's place in the page's layout; the [MapHost] draws the map into it. It leaves
 * the host as its page starts to leave, so the map fades out with the page.
 */
@Composable
fun MapSlot(modifier: Modifier = Modifier, center: GeoPoint? = null, radius: Number = 2) {
    val host = LocalMapHost.current
    val slot = remember { MapSlotState(center, radius.toFloat()) }
    slot.center = center
    slot.radius = radius.toFloat()
    val isLeaving = LocalNavAnimatedContentScope.current.transition.targetState == EnterExitState.PostExit
    DisposableEffect(slot, isLeaving) {
        if (!isLeaving) host.slots.add(slot)
        onDispose { host.slots.remove(slot) }
    }
    Spacer(modifier.onGloballyPositioned { slot.bounds = Rect(it.positionInRoot(), it.size.toSize()) })
}
