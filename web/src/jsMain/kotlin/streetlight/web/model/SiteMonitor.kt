package streetlight.web.model

import kampfire.model.getDataOrNull
import kampfire.model.handleOutcome
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.MetricResolution
import streetlight.model.data.SiteStatus
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.minutes

class SiteMonitor(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(SiteMonitorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val pointsFlow = stateFlow.mapDistinct { it.points }
    val pointFlow = MutableSharedFlow<SiteStatus>()

    init {
        scope.launch {
            launch {
                val points = api.feedSiteStatus().handleOutcome(toaster::toast) ?: emptyList()
                state.set { it.copy(points = points) }
            }
            launch {
                while (true) {
                    delay(1.minutes)
                    val point = api.readLastSiteStatus(MetricResolution.OneMinute).getDataOrNull() ?: continue
                    pointFlow.emit(point)
                }
            }
        }
    }
}

data class SiteMonitorState(
    val points: List<SiteStatus> = emptyList()
)