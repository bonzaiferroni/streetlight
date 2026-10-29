package streetlight.compose.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import koala.model.Toast
import koala.model.Toaster
import kotlinx.coroutines.flow.drop
import org.koin.compose.koinInject
import streetlight.compose.modifier.borderRadius
import streetlight.compose.modifier.pointerClick
import streetlight.compose.utility.collectAsState

/**
 * The toasts of the [Toaster] over the bottom left of the page, above the dock, the newest at the bottom pushing the
 * others up. A click dismisses a toast, and hovering holds it. The cousin of the web's `wireToaster`.
 */
@Composable
fun ToasterHost(modifier: Modifier = Modifier) {
    val theme = currentTheme
    val toaster = koinInject<Toaster>()
    val toasts by toaster.toastsState.collectAsState()
    // a toast stays shown while it fades out after it leaves the toaster
    val shown = remember { mutableStateListOf<Toast>() }
    LaunchedEffect(toasts) {
        shown.addAll(toasts - shown.toSet())
    }

    Column(
        1,
        modifier.padding(bottom = currentPageInsets.calculateBottomPadding())
            .padding(start = theme.unit * 2)
            .widthIn(max = theme.unit * ToastMaxWidthUnits),
    ) {
        shown.forEach { toast ->
            key(toast.id) {
                val visibility = remember { MutableTransitionState(false) }
                visibility.targetState = toast in toasts
                LaunchedEffect(visibility.isIdle, visibility.currentState) {
                    if (visibility.isIdle && !visibility.currentState) shown.remove(toast)
                }
                AnimatedVisibility(
                    visibility,
                    enter = fadeIn(theme.magic.eased()) + expandVertically(theme.magic.eased()),
                    exit = fadeOut(theme.magic.eased()) + shrinkVertically(theme.magic.eased()),
                ) {
                    ToastCard(toast, toaster)
                }
            }
        }
    }
}

/** [toast] on blurred glass, dismissed by a click and held while hovered. */
@Composable
private fun ToastCard(toast: Toast, toaster: Toaster) {
    val hover = remember { MutableInteractionSource() }
    val isHovered by hover.collectIsHoveredAsState()
    LaunchedEffect(toast) {
        snapshotFlow { isHovered }.drop(1).collect {
            if (it) toaster.hold(toast) else toaster.release(toast)
        }
    }

    MessageCard(
        toast.message,
        Modifier.hoverable(hover)
            .pointerClick { toaster.dismiss(toast) }
            .borderRadius(MessageRadiusUnits)
            .hazeBlur(HazeInput.Sources(LocalPageHaze.current), ToastBlur),
    )
}

/** The blur of the page behind a toast, the web's `BlurBackdrop`. */
private val ToastBlur = HazeBlurStyle { blurRadius(10.dp) }

/** The widest a toast grows in units. */
private const val ToastMaxWidthUnits = 60
