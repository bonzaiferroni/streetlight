package koala.dom

import koala.html.OverlayId
import koala.html.Id
import kotlinx.browser.document
import kotlinx.html.js.div

fun AppScope.mountFullscreen(
    id: Id,
    block: AppScope.() -> Unit
) {
    val element = div { }

    val fullscreenMount = document.getElementById(OverlayId.mount)

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

private val fullscreenCache = mutableMapOf<Id, RenderJob>()
private var currentCache: RenderJob? = null