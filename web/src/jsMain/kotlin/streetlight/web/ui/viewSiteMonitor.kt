package streetlight.web.ui

import koala.SiteImage
import koala.modifier.*
import koala.dom.*
import koala.external.ECharts
import streetlight.model.ui.SiteMonitorRoute
import streetlight.web.pages.appHeader
import web.dom.document

fun RouteScope.viewSiteMonitor() {
    val model = app.getSiteMonitor(contentScope)
    column(BodyStyle.ShellColumn) {
        appHeader()
        column(BodyStyle.MainColumn) {
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
            appFooter(sourcePathUi("viewSiteMonitor.kt"))
        }
    }

    document.setTitle(SiteMonitorRoute)
}