package streetlight.compose.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The [tabs] by name, a button for each above the content of the chosen one, the first at the start. The cousin of
 * koala's `tabs`.
 */
@Composable
fun Tabs(tabs: List<Pair<String, @Composable () -> Unit>>, modifier: Modifier = Modifier) {
    var chosen by rememberSaveable { mutableIntStateOf(0) }
    Column(1, modifier.fillMaxWidth()) {
        Row(1, Modifier.align(Alignment.CenterHorizontally)) {
            tabs.forEachIndexed { index, (name, _) ->
                val style = if (index == chosen) ButtonStyle.Primary else ButtonStyle.Zen
                Button(name, onClick = { chosen = index }, style = style)
            }
        }
        tabs.getOrNull(chosen)?.second?.invoke()
    }
}
