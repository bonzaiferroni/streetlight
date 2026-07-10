package streetlight.web.ui

import koala.SiteImage
import koala.css.KoalaTheme
import koala.dom.*
import koala.external.ECharts
import streetlight.model.data.SiteMetric

fun AppScope.viewSiteStatus() {
    val model = app.getSiteMonitor(parentScope)
    column(BodyStyle.Mod) {
        featureHeader("Streetlight Status", "live stats and charts", SiteImage.ControlRoom.url)

        lazyScript(
            "https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js",
             { ECharts.registerTheme(KoalaTheme.ThemeId, ChartUtility.buildTheme()) }
        ) {
            lineChart(
                title = "Status",
                pointsFlow = model.pointsFlow,
                pointFlow = model.pointFlow,
                getX = { it.endedAt.toEpochMilliseconds().toDouble() },
                getY = { it.getMetricOrZero(SiteMetric.RequestCount) },
            )
        }
    }
}