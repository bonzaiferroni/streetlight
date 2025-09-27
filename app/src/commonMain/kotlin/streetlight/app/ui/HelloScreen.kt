package streetlight.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import pondui.ui.controls.Scaffold

@Composable
fun HelloScreen(
) {
    Scaffold {
        ScribeQuestView()
    }
}

