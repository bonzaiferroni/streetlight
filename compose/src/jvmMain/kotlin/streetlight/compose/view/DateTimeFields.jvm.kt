package streetlight.compose.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kampfire.model.MutableTap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// the desktop has no picker of its own
@Composable
actual fun DateField(state: MutableTap<LocalDate?>, modifier: Modifier) = PopoverDateField(state, modifier)

@Composable
actual fun TimeField(state: MutableTap<LocalTime?>, modifier: Modifier, isOptional: Boolean) =
    PopoverTimeField(state, modifier, isOptional)
