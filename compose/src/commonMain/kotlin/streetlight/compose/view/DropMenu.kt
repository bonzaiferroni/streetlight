package streetlight.compose.view

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kampfire.model.Labeled
import kampfire.model.MutableTap
import koala.SvgFile
import streetlight.compose.modifier.padding
import streetlight.compose.utility.collectAsState

/**
 * A button reading the [label] of the value of [state], opening a menu of [options] that sets it, the chosen one
 * checked. The cousin of the web's `dropMenu`.
 */
@Composable
fun <T> DropMenu(state: MutableTap<T>, options: List<T>, label: (T) -> String, modifier: Modifier = Modifier) {
    val value by state.collectAsState()
    var isOpen by remember { mutableStateOf(false) }

    Box(modifier) {
        Button(label(value), onClick = { isOpen = true }, style = ButtonStyle.Zen, flair = SvgFile.ChevronDown)
        Popover(isOpen, onDismiss = { isOpen = false }) {
            Column(1, Modifier.padding(1)) {
                options.forEach { option ->
                    Button(
                        label(option),
                        onClick = {
                            isOpen = false
                            state.set(option)
                        },
                        style = ButtonStyle.Zen,
                        flair = SvgFile.Check.takeIf { option == value },
                    )
                }
            }
        }
    }
}

/** A [DropMenu] of every entry of the enum [E], by its label. */
@Composable
inline fun <reified E> DropMenu(state: MutableTap<E>, modifier: Modifier = Modifier) where E : Enum<E>, E : Labeled =
    DropMenu(state, enumValues<E>().toList(), { it.label }, modifier)
