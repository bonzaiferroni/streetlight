package streetlight.web.dom.page

import koala.modifier.KoalaStyle
import koala.modifier.rgba
import koala.modifier.removeStyle
import koala.modifier.setStyle
import kotlinx.css.pct
import streetlight.model.data.PageBackgroundMode
import streetlight.model.data.PageTheme
import web.dom.document

/** Sets [theme] on the body, or clears the page theme when `null`. */
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
    val lightless = theme?.backgroundMode == PageBackgroundMode.None
    body.setStyle(KoalaStyle.RhoRadius, if (lightless) 0 else theme?.rho?.position?.radius)
    body.setStyle(KoalaStyle.BetaRadius, if (lightless) 0 else theme?.beta?.position?.radius)
    body.setStyle(KoalaStyle.GammaRadius, if (lightless) 0 else theme?.gamma?.position?.radius)
    body.setStyle(KoalaStyle.RhoFocus, theme?.rho?.focus?.pct)
    body.setStyle(KoalaStyle.BetaFocus, theme?.beta?.focus?.pct)
    body.setStyle(KoalaStyle.GammaFocus, theme?.gamma?.focus?.pct)

    when (theme?.backgroundMode) {
        null, PageBackgroundMode.Flux -> body.removeStyle(KoalaStyle.ColorFlux)
        else -> body.setStyle(KoalaStyle.ColorFlux.of("none"))
    }
}