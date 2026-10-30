package streetlight.compose.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import streetlight.model.data.EntityFeed
import streetlight.model.io.ApiClient

/** A page that shows one feed, starting from [page]: its [feed]. */
class FeedPageModel(page: EntityFeed, api: ApiClient, config: AppConfig) : ViewModel() {
    val feed = FeedModel(viewModelScope, api, config.feedModeState, page)
}
