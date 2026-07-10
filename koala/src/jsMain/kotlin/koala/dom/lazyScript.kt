package koala.dom

import koala.css.ModifierSet
import koala.model.ScriptLoader
import kotlinx.html.div

fun AppScope.lazyScript(
    src: String,
    onLoad: suspend () -> Unit = { },
    mod: ModifierSet? = null,
    content: AppScope.() -> Unit
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
            element.replaceStaticRender(app, parentScope, content)
        }
    }
}