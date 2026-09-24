package streetlight.web.ui

import koala.SiteImage
import koala.modifier.*
import koala.dom.*
import koala.external.ECharts
import streetlight.model.ui.SiteMonitorRoute
import web.dom.document

fun RouteScope.viewSiteMonitor() {
    val model = app.getSiteMonitor(contentScope)
    mainBody("viewSiteMonitor.kt") {
        pageHeader("Streetlight Status", "live stats and charts", SiteImage.ControlRoom)

        lazyScript(
            "https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js",
            { ECharts.registerTheme(KoalaTheme.ThemeId, ChartUtility.buildTheme()) }
        ) {
            column {
                dropMenu(model.timeFrameState, mod = AlignSelfStart)
                lineChart(
                    title = "Status",
                    dataFlow = model.chartFlow,
                    pointFlow = model.pointFlow,
                )
            }
        }
    }

    document.setTitle(SiteMonitorRoute)
}
