package streetlight.web.ui

import koala.SiteImage
import koala.css.*
import koala.dom.*
import koala.external.ECharts
import kotlinx.browser.document
import streetlight.model.ui.SiteMonitorRoute

fun ViewScope.viewSiteMonitor() {
    val model = app.getSiteMonitor(scope)
    column(BodyStyle.Mod) {
        featureHeader("Streetlight Status", "live stats and charts", SiteImage.ControlRoom)

        lazyScript(
            "https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js",
             { ECharts.registerTheme(KoalaTheme.ThemeId, ChartUtility.buildTheme()) }
        ) {
            column {
                dropMenu(model::setTimeFrame, { it.label}, model.timeFrameFlow, mod = modify(AlignSelfStart))
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