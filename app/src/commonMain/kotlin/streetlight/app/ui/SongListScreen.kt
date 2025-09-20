package streetlight.app.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pondui.ui.controls.Button
import pondui.ui.controls.FlowRow
import pondui.ui.controls.Scaffold
import pondui.ui.controls.Text
import pondui.ui.controls.TextField
import streetlight.app.SongListRoute
import streetlight.app.generated.resources.Res

@OptIn(ExperimentalLayoutApi::class, ExperimentalResourceApi::class)
@Composable
fun SongListScreen(
    route: SongListRoute,
    viewModel: SongListModel = viewModel { SongListModel(route) }
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            FlowRow(1) {
                TextField(
                    state.newName,
                    onValueChange = viewModel::setNewName,
                    placeholder = "Name",
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    state.newArtist,
                    onValueChange = viewModel::setNewArtist,
                    placeholder = "Artist",
                    modifier = Modifier.weight(1f)
                )
                Button("Create", onClick = viewModel::createItem, modifier = Modifier.fillMaxRowHeight())
            }
        }
        LazyColumn {
            items(state.songs) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(it.name)
                    Button("👉", onClick = { })
                }
            }
        }

        Spirit("happy-fox")
    }
}