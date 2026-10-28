package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import streetlight.compose.modifier.padding

/**
 * The numbered [labels] of a staged form, small and in capitals, wrapping where they do not fit: the [current] stage in
 * the primary's foreground, a later one at the ink's high opacity, and an earlier one at half, readable still, since a
 * stage can be revisited. The cousin of the web's `onboardStage`, without its strikethrough.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StageHeader(labels: List<String>, current: Int, modifier: Modifier = Modifier) {
    val theme = currentTheme
    val colors = theme.colors
    FlowRow(
        modifier.fillMaxWidth().padding(2),
        horizontalArrangement = Arrangement.spacedBy(theme.unit * 2, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(theme.unit),
    ) {
        labels.forEachIndexed { index, label ->
            BasicText(
                "${index + 1}. $label".uppercase(),
                style = theme.text.small,
                color = {
                    when {
                        index == current -> colors.primaryFg
                        index > current -> colors.ink.copy(alpha = theme.opacity.high)
                        else -> colors.ink.copy(alpha = theme.opacity.half)
                    }
                },
            )
        }
    }
}
