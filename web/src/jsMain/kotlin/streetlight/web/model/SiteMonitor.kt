package streetlight.web.model

import kampfire.model.Labeled
import kampfire.model.toDataOr
import koala.model.ChartData
import koala.model.ChartPoint
import koala.model.seriesOf
import koala.utils.launch
import koala.model.dedup
import kampfire.model.mutableTapOf
import kampfire.model.reactIn
import kampfire.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
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

    private val statusFlow = MutableSharedFlow<SiteStatus>()
    val timeFrameState = state.mutableTapOf({ it.timeFrame }) { copy(timeFrame = it) }
    val chartFlow = stateFlow.dedup { state ->
        ChartData(
            series = state.metrics.map { metric ->
                seriesOf(
                    name = metric.label,
                    source = state.points.asReversed(),
                    getX = ::timeOf,
                    getY = { it.getMetricOrZero(metric) },
                    axisLabel = metric.metricAxis?.label ?: metric.label,
                )
            }
        )
    }
    val pointFlow = statusFlow.map { point ->
        stateNow.metrics.map { metric -> ChartPoint(timeOf(point), point.getMetricOrZero(metric)) }
    }

    var refreshJob: Job? = null

    init {
        timeFrameState.reactIn(scope) {
            refreshData()
        }
    }

    private fun refreshData() {
        refreshJob?.cancel()
        refreshJob = scope.launch(::refreshData) {
            val feed = api.feedSiteStatusFeed(stateNow.timeFrame.resolution).toDataOr(toaster) { return@launch }
            state.set { copy(points = feed.points) }
            while (true) {
                delay(stateNow.timeFrame.resolution.duration)
                val status = api.readLastSiteStatus(stateNow.timeFrame.resolution).toDataOr{ continue }
                statusFlow.emit(status)
            }
        }
    }
}

private fun timeOf(point: SiteStatus) = point.endedAt.toEpochMilliseconds().toDouble()

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
