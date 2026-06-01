package streetlight.web.ui

import kabinet.utils.toMetricString
import koala.core.queryFirstOrNull
import koala.dom.getAncestor
import koala.dom.getAttribute
import koala.dom.toggle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import streetlight.model.data.LightEdit
import streetlight.web.io.ApiClient
import streetlight.web.layouts.LightControl
import kotlin.uuid.Uuid

class LightService(val scope: CoroutineScope, val api: ApiClient) {

    fun toggleLight(element: HTMLElement, postId: String) {
        val uuid = Uuid.parseOrNull(postId) ?: error("uuid not found")
        val base = element.getAncestor(LightControl.Class) ?: error("ancestor not found")
        val lightType = base.getAttribute(LightControl.TypeData) ?: error("light type not found")
        val counter = base.queryFirstOrNull(LightControl.Counter) ?: error("counter not found")
        val isLit = base.toggle(LightControl.Lit)
        counter.textContent?.toIntOrNull()?.let {
            val count = if (isLit) it + 1 else it - 1
            counter.textContent = count.toMetricString()
        }
        scope.launch {
            api.editLight(LightEdit(uuid, isLit, lightType))
        }
    }
}