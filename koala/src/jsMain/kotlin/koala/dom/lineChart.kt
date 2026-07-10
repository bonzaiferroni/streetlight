package koala.dom

import koala.css.*
import koala.model.LineChart
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

fun TagScope.lineChart() {
    val element = div(modify(Width64, Height48))
    val chart = LineChart<Pair<Instant, Int>>(
        element,
        "Yer data",
        { it.first.toEpochMilliseconds().toDouble() },
        { it.second.toDouble() }
    )
    val data = mockSiteStatus()
    chart.render(data)
}

fun mockSiteStatus(
    points: Int = 48,
    interval: Duration = 30.minutes,
    end: Instant = Clock.System.now()
): List<Pair<Instant, Int>> {
    val start = end - interval * (points - 1)
    var value = 40
    return List(points) { i ->
        value = (value + Random.nextInt(-8, 12)).coerceIn(0, 120)
        (start + interval * i) to value
    }
}