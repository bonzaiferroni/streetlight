package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField

@Composable
fun HelloScreen(
) {
    var text by remember { mutableStateOf("") }
    Scaffold {

        val scope = remember { ContentScope() }
        scope.contentBox {
            Text(text)
        }

        Box() {
            scope.content?.invoke()
        }

        TextField(text) { text = it }
    }
}

fun ContentScope.contentBox(
    content: @Composable () -> Unit
) {
    addContent(content)
}

class ContentScope {
    var content: (@Composable () -> Unit)? = null

    fun addContent(content: @Composable () -> Unit) {
        this.content = content
    }
}