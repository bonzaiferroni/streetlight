package streetlight.web.interop

import kabinet.utils.toMetricString
import kampfire.model.toDataOrNull
import koala.core.queryFirstOrNull
import koala.dom.ViewScope
import koala.dom.getAttribute
import koala.dom.getClosest
import koala.dom.querySelectorAll
import koala.dom.requireAttribute
import koala.dom.setAttribute
import koala.dom.toggle
import koala.html.Attribute
import koala.interop.KtFunction
import streetlight.model.data.LightEdit
import streetlight.model.data.MarkUpdate
import streetlight.web.layouts.LightControl
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.CuratorMenu
import streetlight.web.ui.api
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement
import kotlin.uuid.Uuid

fun ViewScope.appGlobalFunctions() = listOf(
    KtFunction(LightControl.ToggleFun, this::toggleLight),
    KtFunction(AppFun.UpdateMark, this::updateMark)
)

fun ViewScope.toggleLight(element: HTMLElement, postId: String) {
    val uuid = Uuid.parseOrNull(postId) ?: error("uuid not found")
    val base = element.getClosest(LightControl.Class) ?: error("ancestor not found")
    val lightType = base.getAttribute(LightControl.TypeData) ?: error("light type not found")
    val counter = base.queryFirstOrNull(LightControl.Counter) ?: error("counter not found")
    val isLit = base.toggle(LightControl.Lit)
    counter.textContent?.toIntOrNull()?.let {
        val count = if (isLit) it + 1 else it - 1
        counter.textContent = count.toMetricString()
    }
    launchEffect {
        api.editLight(LightEdit(uuid, isLit, lightType))
    }
}