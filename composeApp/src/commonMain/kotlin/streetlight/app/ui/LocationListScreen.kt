package streetlight.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import streetlight.app.chopui.Scaffold

class LocationListScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val screenModel = rememberScreenModel<LocationListModel>()
        val state by screenModel.state
        Scaffold("Locations", navigator) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LazyColumn {
                    items(state.locations) {
                        Text(it.name)
                    }
                }
            }
        }
    }
}