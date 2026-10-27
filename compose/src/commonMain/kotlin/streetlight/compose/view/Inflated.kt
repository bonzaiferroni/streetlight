package streetlight.compose.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.AppRoute
import streetlight.compose.model.Inflation
import streetlight.compose.model.InflationModel
import streetlight.compose.utility.collectAsState

/**
 * Shows [content] with the content of [route] once it is inflated, fading it in when it arrives after the page does; a
 * message while it loads, held back for a moment so a quick read shows none, or when a problem keeps it. A screen calls
 * it with the content type its route reads.
 */
@Composable
inline fun <reified C : Any> Inflated(route: AppRoute, crossinline content: @Composable (C) -> Unit) {
    val model = koinViewModel<InflationModel> { parametersOf(route) }
    val inflation by model.inflationState.collectAsState()
    val magic = currentTheme.magic
    AnimatedContent(
        targetState = inflation,
        contentKey = { it::class },
        transitionSpec = { fadeIn(magic.eased()) togetherWith fadeOut(magic.eased()) },
    ) { state ->
        when (state) {
            Inflation.Loading -> InflationMessage("Loading…", isHeldBack = true)
            is Inflation.Problem -> InflationMessage(state.message)
            is Inflation.Ready -> content(state.content as C)
        }
    }
}

/** A [message] in the middle of the view while its content is not shown, fading in after a moment when [isHeldBack]. */
@Composable
fun InflationMessage(message: String, isHeldBack: Boolean = false) {
    val theme = currentTheme
    var isShown by remember { mutableStateOf(!isHeldBack) }
    if (isHeldBack) LaunchedEffect(Unit) {
        delay(theme.magic.interval * HoldBackIntervals.toLong())
        isShown = true
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedVisibility(isShown, enter = fadeIn(theme.magic.eased()), exit = fadeOut(theme.magic.eased())) {
            BasicText(message, color = { theme.colors.ink })
        }
    }
}

/** The intervals a loading message waits before it shows. */
private const val HoldBackIntervals = 3
