package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class SiteStatus(
    val siteStatusId: SiteStatusId,
    val integers: Map<SiteMetric, Int>,
    val doubles: Map<SiteMetric, Double>,
    val resolution: MetricResolution,
    val coverage: Float,
    val startedAt: Instant,
    val endedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class SiteStatusId(override val value: Uuid): RecordId {
    companion object {
        fun random() = SiteStatusId(Uuid.random())
    }
}

enum class SiteMetric(val metricType: MetricType, val meterName: String) {
    RequestCount(MetricType.Count, "ktor.http.server.requests"),
    ResponseLatencyAverage(MetricType.Average, "ktor.http.server.requests"),
    ResponseLatencyMax(MetricType.Max, "ktor.http.server.requests");
}

enum class MetricType {
    Count,
    Average,
    Max,
}

enum class MetricResolution(val duration: Duration) {
    OneMinute(1.minutes),
    FiveMinutes(5.minutes),
    ThirtyMinutes(30.minutes),
    ThreeHours(3.hours),
    OneDay(24.hours),
    OneWeek(7.days);

    val predecessor: MetricResolution?
        get() = if (ordinal > 0) entries[ordinal - 1] else null
}