package koala.dom

import koala.css.Reveal
import koala.html.FullscreenId
import koala.html.Id
import kotlinx.browser.document
import kotlinx.dom.clear
import kotlinx.html.js.div

fun RenderContext.mountFullscreen(
    id: Id,
    block: RenderContext.() -> Unit
) {
    val element = div { }

    val fullscreenMount = document.getElementById(FullscreenId.mount)

//    onLoad {
//        fullscreenMount.clear()
//        val cache = fullscreenCache[id]?.also {
//            fullscreenMount.append(it.elements)
//        } ?: createRender(fullscreenMount, block)
//        cache.context.emitOnLoad()
//        fullscreenMount.modify(Reveal)
//        currentCache = cache
//    }
//
//    onUnload {
//        currentCache?.context?.emitOnUnload()
//        fullscreenMount.unmodify(Reveal)
//    }
}

private val fullscreenCache = mutableMapOf<Id, RenderCache>()
private var currentCache: RenderCache? = null