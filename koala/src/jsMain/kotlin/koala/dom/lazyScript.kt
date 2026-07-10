package koala.dom

import koala.model.ScriptLoader
import kotlinx.html.div

fun AppScope.lazyScript(
    src: String,
    onLoad: suspend () -> Unit = { },
    content: AppScope.() -> Unit
) {
    val isLoaded = ScriptLoader.isLoaded(src)
    val element = div {
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