package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.data.GalaxyContent
import koala.model.Toaster
import streetlight.model.io.ApiClient

/** A galaxy's [content]: the galaxy, and its [feed]. */
class GalaxyModel(val content: GalaxyContent, api: ApiClient, toaster: Toaster, config: AppConfig) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, toaster, config.feedModeState, content.feed)
}
