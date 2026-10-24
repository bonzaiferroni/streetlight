package streetlight.compose.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kampfire.model.Tap

/** The value of this tap as Compose state, starting from [Tap.now]. */
@Composable
fun <T> Tap<T>.collectAsState(): State<T> = flow.collectAsState(now)
