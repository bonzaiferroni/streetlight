package streetlight.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.StateFlow

//@Composable
//fun <T> StateFlow<T>?.collectAsStateOrNull(): State<T> {
//    return this?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }
//}