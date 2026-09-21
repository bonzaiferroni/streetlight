package koala.dom

import koala.model.ChartData
import koala.model.ChartPoint
import koala.modifier.*
import koala.model.LineChartAdapter
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

fun ViewScope.lineChart(
    title: String,
    dataFlow: Flow<ChartData>,
    pointFlow: Flow<List<ChartPoint>>?,
    mod: Modifier? = modify(Height(48))
) {
    val element = div(mod)
    val chart = LineChartAdapter(
        container = element,
        title = title,
    )

    launchEffect {
        launch {
            dataFlow.collect(chart::renderData)
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
