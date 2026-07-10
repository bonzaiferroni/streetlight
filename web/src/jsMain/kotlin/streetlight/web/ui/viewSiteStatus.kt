package streetlight.web.ui

import koala.SiteImage
import koala.css.KoalaTheme
import koala.dom.*
import koala.external.ECharts

fun AppScope.viewSiteStatus() {
    column(BodyStyle.Mod) {
        featureHeader("Site Status", "charts and stats", SiteImage.ControlRoom.url)

        lazyScript(
            "https://cdn.jsdelivr.net/npm/echarts@5.5.1/dist/echarts.min.js",
             { ECharts.registerTheme(KoalaTheme.ThemeId, buildTheme()) }
        ) {
            lineChart()
        }
    }
}