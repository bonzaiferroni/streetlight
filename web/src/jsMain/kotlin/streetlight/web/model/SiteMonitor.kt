package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.getDataOrNull
import kampfire.model.handleResponse
import koala.dom.ChartData
import koala.dom.ChartLine
import koala.model.tap
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import streetlight.model.data.MetricResolution
import streetlight.model.data.SiteMetric
import streetlight.model.data.SiteStatus
import streetlight.web.io.ApiClient

class SiteMonitor(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(SiteMonitorState())
    val stateNow get() = state.now
    val stateFlow = state.flow

    val pointsFlow = stateFlow.tap { it.points }
    val pointFlow = MutableSharedFlow<SiteStatus>()
    val timeFrameFlow = stateFlow.tap { it.timeFrame }
    val dataFlow = stateFlow.tap { state ->
        ChartData(
            points = state.points,
            lines = state.metrics.map { metric ->
                ChartLine(
                    name = metric.label,
                    getX = { it.endedAt.toEpochMilliseconds().toDouble() },
                    getY = { it.getMetricOrZero(metric) },
                    axisLabel = metric.metricAxis?.label ?: metric.label,
                )
            }
        ) }

    var refreshJob: Job? = null

    init {
        refreshData()
    }

    fun setTimeFrame(value: TimeFrame) {
        state.set { it.copy(timeFrame = value) }
        refreshData()
    }

    private fun refreshData() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            val points = api.feedSiteStatus(stateNow.timeFrame.resolution).handleResponse(toaster::toast) ?: emptyList()
            state.set { it.copy(points = points) }
            while (true) {
                delay(stateNow.timeFrame.resolution.duration)
                val point = api.readLastSiteStatus(stateNow.timeFrame.resolution).getDataOrNull() ?: continue
                pointFlow.emit(point)
            }
        }
    }
}

data class SiteMonitorState(
    val points: List<SiteStatus> = emptyList(),
    val metrics: Set<SiteMetric> = SiteMetric.entries.toSet(),
    val timeFrame: TimeFrame = TimeFrame.Hour
)

enum class TimeFrame(val resolution: MetricResolution): Labeled {
    Hour(MetricResolution.OneMinute),
    Day(MetricResolution.ThirtyMinutes),
    Week(MetricResolution.ThreeHours),
    Month(MetricResolution.OneDay),
    Year(MetricResolution.OneWeek);

    override val label = name
}