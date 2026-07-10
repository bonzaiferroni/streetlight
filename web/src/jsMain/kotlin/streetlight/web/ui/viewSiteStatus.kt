package streetlight.web.ui

import koala.SiteImage
import koala.css.*
import koala.dom.*
import koala.external.ECharts

fun AppScope.viewSiteStatus() {
    val model = app.getSiteMonitor(parentScope)
    column(BodyStyle.Mod) {
        featureHeader("Streetlight Status", "live stats and charts", SiteImage.ControlRoom.url)

        lazyScript(
            "https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js",
             { ECharts.registerTheme(KoalaTheme.ThemeId, ChartUtility.buildTheme()) }
        ) {
            column {
                dropMenu(model::setTimeFrame, { it.label}, model.timeFrameFlow, modifiers = modify(AlignSelfStart))
                lineChart(
                    title = "Status",
                    dataFlow = model.dataFlow,
                    pointFlow = model.pointFlow,
                )
            }
        }
    }
}