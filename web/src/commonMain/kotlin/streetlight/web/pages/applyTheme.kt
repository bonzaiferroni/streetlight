package streetlight.web.pages

import koala.modifier.KoalaStyle
import koala.modifier.rgba
import koala.modifier.setStyle
import kotlinx.css.pct
import kotlinx.html.BODY
import streetlight.model.data.PageTheme

/** Sets the colors and gradients of [theme] as the body's custom properties. */
fun BODY.applyTheme(theme: PageTheme) {
    setStyle(
        KoalaStyle.Accent.of(theme.accent),
        KoalaStyle.Primary.of(theme.primary),
        KoalaStyle.RhoColor.of(theme.rho.rgba()),
        KoalaStyle.BetaColor.of(theme.beta.rgba()),
        KoalaStyle.GammaColor.of(theme.gamma.rgba()),
        KoalaStyle.RhoX.of(theme.rho.position.x.pct),
        KoalaStyle.BetaX.of(theme.beta.position.x.pct),
        KoalaStyle.GammaX.of(theme.gamma.position.x.pct),
        KoalaStyle.RhoY.of(theme.rho.position.y.pct),
        KoalaStyle.BetaY.of(theme.beta.position.y.pct),
        KoalaStyle.GammaY.of(theme.gamma.position.y.pct),
        KoalaStyle.RhoRadius.of(theme.rho.position.radius),
        KoalaStyle.BetaRadius.of(theme.beta.position.radius),
        KoalaStyle.GammaRadius.of(theme.gamma.position.radius),
        KoalaStyle.RhoFocus.of(theme.rho.focus.pct),
        KoalaStyle.BetaFocus.of(theme.beta.focus.pct),
        KoalaStyle.GammaFocus.of(theme.gamma.focus.pct),
        if (!theme.colorFlux) KoalaStyle.ColorFlux.of("none") else null
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