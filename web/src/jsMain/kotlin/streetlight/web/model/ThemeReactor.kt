package streetlight.web.model

import koala.css.Rgb
import koala.css.Property
import koala.dom.setProperty
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement

class ThemeReactor(
    scope: CoroutineScope,
    config: SiteConfig
) {
    val root = document.documentElement as? HTMLElement ?: error("documentElement be null")
    // val initialPaper = window.getComputedStyle(root).getPropertyValue(ThemeProperty.paper.expression)
    // val initialInk = window.getComputedStyle(root).getPropertyValue(ThemeProperty.ink.expression)

    init {

        scope.launch {
            config.themeFlow.collect { theme ->
                setTheme(theme)
            }
        }
    }

    private fun setTheme(theme: SiteTheme) {
//
//        when (theme) {
//            SiteTheme.Dark -> {
//                root.style.setProperty(ThemeProperty.paper.expression, initialPaper)
//                root.style.setProperty(ThemeProperty.ink.expression, initialInk)
//                // root.style.setProperty(ThemeProperty.paper.to(ThemeValue.black))
//                // root.style.setProperty(ThemeProperty.ink.to(ThemeValue.white))
//            }
//            SiteTheme.Light -> {
//                root.style.setProperty(ThemeProperty.paper.expression, initialInk)
//                root.style.setProperty(ThemeProperty.ink.expression, initialPaper)
//                // root.style.setProperty(ThemeProperty.paper.to(ThemeValue.white))
//                // root.style.setProperty(ThemeProperty.ink.to(ThemeValue.black))
//            }
//        }
    }
}

object ThemeProperty {
    val paper = Property<Rgb>("paper")
    val ink = Property<Rgb>("ink")
}

object ThemeValue {
    val black = Rgb(9, 13, 13)
    val white = Rgb(240, 246, 246)
}