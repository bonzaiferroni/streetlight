package streetlight.compose.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import kampfire.model.MutableTap
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import streetlight.compose.model.currentPortal
import streetlight.compose.modifier.Outline
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.filigree
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/** A dialog's open [state], its [title], and the [content] the [DialogHost] shows. */
class DialogMountState(val state: MutableTap<Boolean>, title: String?, content: @Composable () -> Unit) {
    var title by mutableStateOf(title)
    var content by mutableStateOf(content)

    /** Closes the dialog through its state. */
    fun close() = state.set(false)
}

/** The one dialog open in the app, if any. */
class DialogHostState {
    var active by mutableStateOf<DialogMountState?>(null)
        private set

    /** Shows [mount], closing the dialog open before it. */
    fun open(mount: DialogMountState) {
        active?.takeIf { it !== mount }?.close()
        active = mount
    }

    /** Stops showing [mount], if it is the one shown. */
    fun close(mount: DialogMountState) {
        if (active === mount) active = null
    }
}

/** The [DialogHostState] of the app, shared by the dialogs' mounts and the host in the overlay. */
val LocalDialogHost = staticCompositionLocalOf<DialogHostState> { error("No DialogHost provided") }

/**
 * Shows a dialog of [content], built from scratch, while [state] is `true`, with its [title] in a filigree above it
 * when given. The dialog is drawn by the [DialogHost], over the whole app; it closes when the mount leaves the
 * composition. The cousin of koala's `dialog` with `rawDialogContent`.
 */
@Composable
fun RawDialogMount(state: MutableTap<Boolean>, title: String? = null, content: @Composable () -> Unit) {
    val host = LocalDialogHost.current
    val isOpen by state.collectAsState()
    val mount = remember(state) { DialogMountState(state, title, content) }
    mount.title = title
    mount.content = content
    DisposableEffect(mount, isOpen) {
        if (isOpen) host.open(mount)
        onDispose { host.close(mount) }
    }
}

/**
 * Shows a dialog of [content] on the dialog's card while [state] is `true`, with its [title] in a filigree above it
 * when given. The cousin of koala's `dialog` with `dialogContent`.
 */
@Composable
fun DialogMount(state: MutableTap<Boolean>, title: String? = null, content: @Composable () -> Unit) {
    RawDialogMount(state, title) { DialogCard(content = content) }
}

/**
 * The card of a dialog holding [content], scrolling once it fills most of the space. The cousin of koala's
 * `dialogCard`.
 */
@Composable
fun DialogCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val theme = currentTheme
    BoxWithConstraints(modifier) {
        Box(
            Modifier.fillMaxWidth()
                .heightIn(max = maxHeight * CardHeightShare)
                .borderRadius(2, moonShadow = true, outline = Outline.Mid)
                .background(theme.colors.paper.copy(alpha = CardAlpha))
                .verticalScroll(rememberScrollState())
                .padding(2)
        ) {
            content()
        }
    }
}

/**
 * Draws the open dialog over the whole app: the page blurred beneath a dimming veil, and the dialog's title and content
 * held a tenth of the way down, rising and fading in as it opens. A click on the veil, Escape, or a change of route
 * closes it.
 */
@Composable
fun DialogHost(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val magic = theme.magic
    val host = LocalDialogHost.current
    val portal = currentPortal
    val active = host.active
    // the last dialog stays while it fades out
    var shown by remember { mutableStateOf(active) }
    active?.let { shown = it }

    LaunchedEffect(active) {
        val mount = active ?: return@LaunchedEffect
        snapshotFlow { portal.backStack.lastOrNull() }.drop(1).first()
        mount.close()
    }

    AnimatedVisibility(active != null, modifier, enter = fadeIn(magic.eased()), exit = fadeOut(magic.eased())) {
        val mount = shown ?: return@AnimatedVisibility
        val rise = with(LocalDensity.current) { RiseDistance.roundToPx() }
        val focus = remember { FocusRequester() }
        LaunchedEffect(Unit) { focus.requestFocus() }
        BoxWithConstraints(
            Modifier.fillMaxSize()
                .hazeBlur(HazeInput.Sources(LocalPageHaze.current), VeilBlur)
                .background(Color.Black.copy(alpha = VeilAlpha))
                .pointerInput(mount) { detectTapGestures { mount.close() } }
                .focusRequester(focus)
                .focusable()
                .onKeyEvent { event ->
                    val isEscape = event.type == KeyEventType.KeyUp && event.key == Key.Escape
                    if (isEscape) mount.close()
                    isEscape
                }
                .padding(horizontal = 1),
            contentAlignment = Alignment.TopCenter,
        ) {
            // held from the top, so a change in its height leaves its title in place, with room below for the veil
            Column(
                1,
                Modifier.padding(PaddingValues(top = maxHeight * TopShare))
                    .widthIn(max = DialogMaxWidth)
                    .heightIn(max = maxHeight * HeightShare)
                    .animateEnterExit(
                        enter = slideInVertically(magic.eased()) { rise },
                        exit = slideOutVertically(magic.eased()) { rise },
                    )
                    // the dialog takes its own clicks, so only a click on the veil closes it
                    .pointerInput(Unit) { detectTapGestures { } },
            ) {
                mount.title?.let { title ->
                    BasicText(
                        title,
                        Modifier.fillMaxWidth().filigree(),
                        style = theme.text.heading2.copy(textAlign = TextAlign.Center),
                        color = { theme.colors.ink },
                    )
                }
                mount.content()
            }
        }
    }
}

/** The blur of the page beneath an open dialog, the web's `blur(8px)`. */
private val VeilBlur = HazeBlurStyle { blurRadius(14.dp) }

/** The veil that dims the page beneath an open dialog. */
private const val VeilAlpha = 0.4f

/** The share of the window above a dialog. */
private const val TopShare = 0.1f

/** The share of the window a dialog may take in height, the web's `80vh`. */
private const val HeightShare = 0.8f

/** The widest a dialog runs, the web's `--content-panel-width`. */
private val DialogMaxWidth = 1080.dp

/** The share of the space a dialog's card may take in height, the web's `70vh` of a dialog held to `80vh`. */
private const val CardHeightShare = 0.875f

/** The dialog card's paper, the web's `--dialog-bg`. */
private const val CardAlpha = 0.8f

/** The distance a dialog rises as it opens, the web's `10px`. */
private val RiseDistance = 10.dp
