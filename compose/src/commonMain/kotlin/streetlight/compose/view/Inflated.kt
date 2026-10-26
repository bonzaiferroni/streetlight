package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import streetlight.compose.model.AppRoute
import streetlight.compose.model.Inflation
import streetlight.compose.model.InflationModel
import streetlight.compose.utility.collectAsState

/**
 * Shows [content] with the content of [route] once it is inflated, a message while it loads or when a problem keeps
 * it. A screen calls it with the content type its route reads.
 */
@Composable
inline fun <reified C : Any> Inflated(route: AppRoute, crossinline content: @Composable (C) -> Unit) {
    val model = koinViewModel<InflationModel> { parametersOf(route) }
    val inflation by model.inflationState.collectAsState()
    when (val state = inflation) {
        Inflation.Loading -> InflationMessage("Loading…")
        is Inflation.Problem -> InflationMessage(state.message)
        is Inflation.Ready -> content(state.content as C)
    }
}

/** A [message] in the middle of the view, while its content is not shown. */
@Composable
fun InflationMessage(message: String) {
    val theme = currentTheme
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicText(message, color = { theme.colors.ink })
    }
}
