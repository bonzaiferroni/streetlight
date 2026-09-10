package streetlight.web.interop

import kabinet.utils.toMetricString
import kampfire.model.toDataOr
import koala.core.queryFirstOrNull
import koala.css.OpacityHigh
import koala.dom.ViewScope
import koala.dom.append
import koala.dom.clear
import koala.dom.getAttribute
import koala.dom.closest
import koala.dom.column
import koala.dom.modify
import koala.dom.requireAttribute
import koala.dom.requireClosestAttribute
import koala.dom.toggle
import koala.dom.unmodify
import koala.interop.KtFunction
import streetlight.model.data.LightEdit
import streetlight.web.layouts.FeedSection
import streetlight.web.layouts.LightControl
import streetlight.web.layouts.feedRow
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.api
import streetlight.web.ui.requireElement
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement
import kotlin.uuid.Uuid

fun ViewScope.appGlobalFunctions() = listOf(
    KtFunction(LightControl.ToggleFun, this::toggleLight),
    KtFunction(AppFun.UpdateMark, this::queryAndUpdateMark),
    KtFunction(AppFun.SortByMark, this::sortByMark)
)

fun ViewScope.toggleLight(element: HTMLElement, postId: String) {
    val uuid = Uuid.parseOrNull(postId) ?: error("uuid not found")
    val base = element.closest(LightControl.Class) ?: error("ancestor not found")
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

fun ViewScope.sortByMark(element: HTMLElement) {
    val markId = element.requireAttribute(AppAttribute.MarkId)
    val galaxyId = element.requireClosestAttribute(AppAttribute.GalaxyId)
    val mount = document.requireElement(FeedSection.MountId)
    launchEffect {
        mount.modify(OpacityHigh)
        val feed = api.readPosts(galaxyId, markId).toDataOr(toaster) {
            mount.unmodify(OpacityHigh)
            return@launchEffect
        }
        mount.unmodify(OpacityHigh)
        mount.clear()
        mount.append {
            column(FeedSection.FeedColumnMod) {
                feed.entities.forEach { post ->
                    val curator = feed.curatorOf(post)
                    feedRow(post, true, curator)
                }
            }
        }
    }
}