package streetlight.compose.modifier

import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf
import streetlight.compose.view.KoalaAppTheme
import streetlight.compose.view.LocalKoalaAppTheme

/** The theme a modifier node applies, read where the node is attached. */
internal val CompositionLocalConsumerModifierNode.theme: KoalaAppTheme get() = currentValueOf(LocalKoalaAppTheme)
