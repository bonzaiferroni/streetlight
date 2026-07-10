package koala.dom

import koala.css.*
import koala.model.LineChart
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

fun <T> AppScope.lineChart(
    title: String,
    pointsFlow: Flow<List<T>>,
    pointFlow: Flow<T>?,
    getX: (T) -> Double,
    getY: (T) -> Double,
) {
    val element = div(modify(Width64, Height48))
    val chart = LineChart(
        container = element,
        title = title,
        getX = getX,
        getY = getY,
    )

    launchEffect {
        launch {
            pointsFlow.collect(chart::renderPoints)
        }

        pointFlow?.let {
            launch {
                it.collect(chart::addPoint)
            }
        }
    }
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

// { it.first.toEpochMilliseconds().toDouble() },
//        { it.second.toDouble() }