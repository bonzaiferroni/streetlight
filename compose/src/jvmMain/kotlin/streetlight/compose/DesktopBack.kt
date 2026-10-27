package streetlight.compose

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import streetlight.compose.model.Portal

/** Goes back through this portal on the desktop's back key, Alt+Left, and answers whether it did. */
fun Portal.backOn(event: KeyEvent): Boolean {
    val isBack = event.type == KeyEventType.KeyDown && event.isAltPressed && event.key == Key.DirectionLeft
    if (isBack) back()
    return isBack
}

/** Goes back through [portal] when the mouse's back button is pressed over the content. */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.backOnMouse(portal: Portal) = onPointerEvent(PointerEventType.Press) { event ->
    if (event.button == PointerButton.Back) portal.back()
}
