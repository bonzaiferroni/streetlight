package streetlight.web.pages

import koala.modifier.Class
import koala.modifier.KoalaStyle
import koala.modifier.rgba
import koala.modifier.setStyle
import koala.modifier.styleOf
import kotlinx.css.pct
import kotlinx.html.BODY
import streetlight.model.data.PageTheme

/** Sets the colors and gradients of [theme] as the body's custom properties. */
fun BODY.applyTheme(theme: PageTheme) = setStyle(theme.toStyleSet())

/** The custom properties that set the colors and gradients of this theme. */
fun PageTheme.toStyleSet() = styleOf(
    KoalaStyle.Accent.of(accent),
    KoalaStyle.Primary.of(primary),
    KoalaStyle.RhoColor.of(rho.rgba()),
    KoalaStyle.BetaColor.of(beta.rgba()),
    KoalaStyle.GammaColor.of(gamma.rgba()),
    KoalaStyle.RhoX.of(rho.position.x.pct),
    KoalaStyle.BetaX.of(beta.position.x.pct),
    KoalaStyle.GammaX.of(gamma.position.x.pct),
    KoalaStyle.RhoY.of(rho.position.y.pct),
    KoalaStyle.BetaY.of(beta.position.y.pct),
    KoalaStyle.GammaY.of(gamma.position.y.pct),
    KoalaStyle.RhoRadius.of(rho.position.radius),
    KoalaStyle.BetaRadius.of(beta.position.radius),
    KoalaStyle.GammaRadius.of(gamma.position.radius),
    KoalaStyle.RhoFocus.of(rho.focus.pct),
    KoalaStyle.BetaFocus.of(beta.focus.pct),
    KoalaStyle.GammaFocus.of(gamma.focus.pct),
    if (!colorFlux) KoalaStyle.ColorFlux.of("none") else null
)

/** The `localStorage` key of the viewer's default theme, held as the root's inline style. */
const val DEFAULT_THEME_KEY = "streetlight.default-theme-style"

/** A root modifier that keeps pages on the viewer's default theme, ignoring their own. */
val DefaultThemeOnly = Class("default-theme-only")

// language="CSS"
val PageThemeCss get() = """
/* the body inherits the default theme from the root, over a page theme's inline style */
:root$DefaultThemeOnly body {
${KoalaStyle.all.joinToString("\n") { "    $it: inherit !important;" }}
}
"""

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