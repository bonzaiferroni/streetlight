package streetlight.web.model

import kampfire.model.getDataOrNull
import kampfire.model.handleOutcome
import koala.dom.ChartData
import koala.dom.ChartLine
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.MetricResolution
import streetlight.model.data.SiteMetric
import streetlight.model.data.SiteStatus
import streetlight.web.io.ApiClient
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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
    val dataFlow = stateFlow.mapDistinct { state ->
        ChartData(
            points = state.points,
            lines = state.metrics.map { metric ->
                ChartLine(
                    getX = { it.endedAt.toEpochMilliseconds().toDouble() },
                    getY = { it.getMetricOrZero(metric) },
                    name = metric.label
                )
            }
        ) }

    init {
        scope.launch {
            launch {
                val points = api.feedSiteStatus().handleOutcome(toaster::toast) ?: emptyList()
                state.set { it.copy(points = points) }
            }
            launch {
                while (true) {
                    delay(50.seconds)
                    console.log("ready")
                    delay(10.seconds)
                    val point = api.readLastSiteStatus(MetricResolution.OneMinute).getDataOrNull() ?: continue
                    pointFlow.emit(point)
                    console.log("emit")
                }
            }
        }
    }
}

data class SiteMonitorState(
    val points: List<SiteStatus> = emptyList(),
    val metrics: Set<SiteMetric> = SiteMetric.entries.toSet()
)