package koala.dom.page

import koala.modifier.*
import koala.model.ScriptLoader
import koala.dom.ViewScope
import koala.dom.launchEffect
import koala.dom.mountChildView
import koala.dom.component.div

/**
 * Loads the script at [src] once, then builds [content]. Content is built at once when the script is already
 * loaded.
 */
fun ViewScope.lazyScript(
    src: String,
    onLoad: suspend () -> Unit = { },
    mod: Modifier? = null,
    content: ViewScope.() -> Unit
) {
    val isLoaded = ScriptLoader.isLoaded(src)
    val element = div(mod) {
        if (isLoaded) {
            content()
        }
    }

    if (!isLoaded) {
        launchEffect {
            ScriptLoader.load(src)
            onLoad()
            this@lazyScript.mountChildView("lazy-script", element, content)
        }
    }
}