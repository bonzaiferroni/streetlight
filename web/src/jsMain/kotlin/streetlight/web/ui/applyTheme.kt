package streetlight.web.ui

import koala.css.KoalaStyle
import koala.css.KoalaTheme
import koala.css.rgba
import koala.dom.ViewScope
import koala.dom.modify
import koala.dom.removeStyle
import koala.dom.setStyle
import koala.dom.unmodify
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.css.pct
import streetlight.model.data.PageTheme
import web.dom.document
import kotlin.time.Duration.Companion.seconds

fun applyTheme(theme: PageTheme?) {
    val body = document.body
    body.setStyle(KoalaStyle.Accent, theme?.accent)
    body.setStyle(KoalaStyle.Primary, theme?.primary)
    body.setStyle(KoalaStyle.RhoColor, theme?.rho?.rgba())
    body.setStyle(KoalaStyle.BetaColor, theme?.beta?.rgba())
    body.setStyle(KoalaStyle.GammaColor, theme?.gamma?.rgba())
    body.setStyle(KoalaStyle.RhoX, theme?.rho?.position?.x?.pct)
    body.setStyle(KoalaStyle.BetaX, theme?.beta?.position?.x?.pct)
    body.setStyle(KoalaStyle.GammaX, theme?.gamma?.position?.x?.pct)
    body.setStyle(KoalaStyle.RhoY, theme?.rho?.position?.y?.pct)
    body.setStyle(KoalaStyle.BetaY, theme?.beta?.position?.y?.pct)
    body.setStyle(KoalaStyle.GammaY, theme?.gamma?.position?.y?.pct)
    body.setStyle(KoalaStyle.RhoRadius, theme?.rho?.position?.radius)
    body.setStyle(KoalaStyle.BetaRadius, theme?.beta?.position?.radius)
    body.setStyle(KoalaStyle.GammaRadius, theme?.gamma?.position?.radius)
    body.setStyle(KoalaStyle.RhoFocus, theme?.rho?.focus?.pct)
    body.setStyle(KoalaStyle.BetaFocus, theme?.beta?.focus?.pct)
    body.setStyle(KoalaStyle.GammaFocus, theme?.gamma?.focus?.pct)

    when (theme?.colorFlux) {
        false -> body.setStyle(KoalaStyle.ColorFlux.to("none"))
        else -> body.removeStyle(KoalaStyle.ColorFlux)
    }
}