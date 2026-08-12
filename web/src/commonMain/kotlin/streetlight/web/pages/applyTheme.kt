package streetlight.web.pages

import koala.css.KoalaStyle
import koala.css.rgba
import koala.css.setStyle
import kotlinx.css.pct
import kotlinx.html.BODY
import streetlight.model.data.PageTheme

fun BODY.applyTheme(theme: PageTheme) {
    setStyle(
        KoalaStyle.Accent.to(theme.accent),
        KoalaStyle.Primary.to(theme.primary),
        KoalaStyle.RhoColor.to(theme.rho.rgba()),
        KoalaStyle.BetaColor.to(theme.beta.rgba()),
        KoalaStyle.GammaColor.to(theme.gamma.rgba()),
        KoalaStyle.RhoX.to(theme.rho.position.x.pct),
        KoalaStyle.BetaX.to(theme.beta.position.x.pct),
        KoalaStyle.GammaX.to(theme.gamma.position.x.pct),
        KoalaStyle.RhoY.to(theme.rho.position.y.pct),
        KoalaStyle.BetaY.to(theme.beta.position.y.pct),
        KoalaStyle.GammaY.to(theme.gamma.position.y.pct),
        KoalaStyle.RhoRadius.to(theme.rho.position.radius),
        KoalaStyle.BetaRadius.to(theme.beta.position.radius),
        KoalaStyle.GammaRadius.to(theme.gamma.position.radius),
        KoalaStyle.RhoFocus.to(theme.rho.focus.pct),
        KoalaStyle.BetaFocus.to(theme.beta.focus.pct),
        KoalaStyle.GammaFocus.to(theme.gamma.focus.pct),
        if (!theme.colorFlux) KoalaStyle.ColorFlux.to("none") else null
    )
}

// fun applyTheme(theme: PageTheme?) {
//    val body = document.body ?: return
//    body.setStyle(KoalaStyle.Accent, theme?.accent)
//    body.setStyle(KoalaStyle.Primary, theme?.primary)
//    body.setStyle(KoalaStyle.RhoColor, theme?.rho?.rgba())
//    body.setStyle(KoalaStyle.BetaColor, theme?.beta?.rgba())
//    body.setStyle(KoalaStyle.GammaColor, theme?.gamma?.rgba())
//    body.setStyle(KoalaStyle.RhoX, theme?.rho?.position?.x?.pct)
//    body.setStyle(KoalaStyle.BetaX, theme?.beta?.position?.x?.pct)
//    body.setStyle(KoalaStyle.GammaX, theme?.gamma?.position?.x?.pct)
//    body.setStyle(KoalaStyle.RhoY, theme?.rho?.position?.y?.pct)
//    body.setStyle(KoalaStyle.BetaY, theme?.beta?.position?.y?.pct)
//    body.setStyle(KoalaStyle.GammaY, theme?.gamma?.position?.y?.pct)
//    body.setStyle(KoalaStyle.RhoRadius, theme?.rho?.position?.radius)
//    body.setStyle(KoalaStyle.BetaRadius, theme?.beta?.position?.radius)
//    body.setStyle(KoalaStyle.GammaRadius, theme?.gamma?.position?.radius)
//    body.setStyle(KoalaStyle.RhoFocus, theme?.rho?.focus?.pct)
//    body.setStyle(KoalaStyle.BetaFocus, theme?.beta?.focus?.pct)
//    body.setStyle(KoalaStyle.GammaFocus, theme?.gamma?.focus?.pct)
//
//    when (theme?.colorFlux) {
//        false -> body.setStyle(KoalaStyle.ColorFlux.to("none"))
//        else -> body.removeStyle(KoalaStyle.ColorFlux)
//    }
//}