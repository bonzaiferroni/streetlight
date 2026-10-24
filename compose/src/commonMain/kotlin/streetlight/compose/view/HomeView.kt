package streetlight.compose.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import streetlight.compose.model.HomeModel
import streetlight.compose.utility.collectAsState
import streetlight.model.data.Entity

/** The view of [streetlight.compose.model.HomeRoute]: the home feed, each entity as its thumbnail and label. */
context(model: HomeModel)
@Composable
fun HomeView() {
    val entities by model.entitiesState.collectAsState()
    val isLoading by model.isLoadingState.collectAsState()
    val problem by model.problemState.collectAsState()
    val colors = LocalAppColors.current

    val message = problem ?: if (isLoading) "Loading…" else null
    if (message != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BasicText(message, color = { colors.ink })
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(entities) { entity -> EntityRow(entity) }
    }
}

/** One entity of the feed: its thumbnail, when it has an image, and its label. */
@Composable
private fun EntityRow(entity: Entity) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = entity.image?.let { it.thumb ?: it.url },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp),
        )
        BasicText(entity.label, color = { colors.ink })
    }
}
