package koala.model

import kotlinx.browser.document
import kotlinx.coroutines.CompletableDeferred
import org.w3c.dom.HTMLScriptElement

object ScriptLoader {
    private val loaded = mutableMapOf<String, CompletableDeferred<Unit>>()

    fun isLoaded(src: String): Boolean = loaded[src]?.let { it.isCompleted && !it.isCancelled } == true

    suspend fun load(src: String) {
        loaded.getOrPut(src) {
            val deferred = CompletableDeferred<Unit>()
            val el = document.createElement("script") as HTMLScriptElement
            el.src = src
            el.async = true
            el.onload = { deferred.complete(Unit) }
            el.onerror = { _, _, _, _, _ ->
                deferred.completeExceptionally(IllegalStateException("Failed to load $src"))
            }
            document.head?.appendChild(el)
            deferred
        }.await()
    }
}