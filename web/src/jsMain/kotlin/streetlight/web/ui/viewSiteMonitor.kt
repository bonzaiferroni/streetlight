package streetlight.web.ui

import koala.SiteImage
import koala.css.*
import koala.dom.*
import koala.external.ECharts
import kotlinx.browser.document
import streetlight.model.ui.SiteMonitorRoute

fun RouteScope.viewSiteMonitor() {
    val model = app.getSiteMonitor(contentScope)
    column(BodyStyle.Mod) {
        featureHeader("Streetlight Status", "live stats and charts", SiteImage.ControlRoom)

        lazyScript(
            "https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js",
             { ECharts.registerTheme(KoalaTheme.ThemeId, ChartUtility.buildTheme()) }
        ) {
            column {
                dropMenu(model.timeFrameField, mod = modify(AlignSelfStart))
                lineChart(
                    title = "Status",
                    dataFlow = model.dataFlow,
                    pointFlow = model.pointFlow,
                )
            }
        }
    }

    document.setTitle(SiteMonitorRoute)
}