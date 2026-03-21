package streetlight.web.model

import koala.css.RgbValue
import koala.css.StyleProperty
import koala.dom.setProperty
import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement

class ThemeReactor(
    scope: CoroutineScope,
    config: SiteConfig
) {
    init {
        scope.launch {
            config.themeFlow.collect { theme ->
                setTheme(theme)
            }
        }
    }

    private fun setTheme(theme: SiteTheme) {
        val root = document.documentElement as? HTMLElement ?: error("documentElement be null")

        when (theme) {
            SiteTheme.Dark -> {
                root.style.setProperty(ThemeProperty.paper, ThemeValue.black)
                root.style.setProperty(ThemeProperty.ink, ThemeValue.white)
            }
            SiteTheme.Light -> {
                root.style.setProperty(ThemeProperty.paper, ThemeValue.white)
                root.style.setProperty(ThemeProperty.ink, ThemeValue.black)
            }
        }
    }
}

object ThemeProperty {
    val paper = StyleProperty("paper")
    val ink = StyleProperty("ink")
}

object ThemeValue {
    val black = RgbValue(9, 13, 13)
    val white = RgbValue(240, 246, 246)
}