package streetlight.compose.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import streetlight.compose.model.HomeModel
import streetlight.compose.utility.collectAsState

/** The view of [streetlight.compose.model.HomeRoute]: the home feed, each entity a [FeedRow]. */
context(model: HomeModel)
@Composable
fun HomeView() {
    val entities by model.entitiesState.collectAsState()
    val isLoading by model.isLoadingState.collectAsState()
    val problem by model.problemState.collectAsState()
    val theme = currentTheme

    val message = problem ?: if (isLoading) "Loading…" else null
    if (message != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BasicText(message, color = { theme.colors.ink })
        }
        return
    }

    LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(entities) { index, entity ->
            if (index > 0) Divider()
            FeedRow(entity)
        }
    }
}

/** The hairline between two rows. */
@Composable
private fun Divider() {
    val theme = currentTheme
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(theme.colors.outlineLow))
}
