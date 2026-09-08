package streetlight.web.interop

import kabinet.utils.toMetricString
import kampfire.model.toDataOrNull
import koala.dom.ViewScope
import koala.dom.getAttribute
import koala.dom.querySelectorAll
import koala.dom.requireAttribute
import koala.dom.setAttribute
import koala.html.Attribute
import streetlight.model.data.MarkUpdate
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.CuratorMenu
import streetlight.web.ui.api
import streetlight.web.ui.toaster
import web.dom.document
import web.html.HTMLElement

fun ViewScope.updateMark(element: HTMLElement) {
    val markId = element.requireAttribute(CuratorMenu.MarkButtonId)
    val lean = element.requireAttribute(CuratorMenu.Lean)
    val isMarked = !element.requireAttribute(Attribute.IsOn)
    val postId = element.requireAttribute(AppAttribute.PostId)

    // modify source element
    element.setAttribute(Attribute.IsOn.to(isMarked))

    // modify tally text
    document.querySelectorAll(CuratorMenu.MarkTallyId.to(markId)).forEach {
        if (it.requireAttribute(AppAttribute.PostId) != postId) return@forEach
        val tally = it.requireAttribute(CuratorMenu.MarkTally) + if (isMarked) lean.value else -lean.value
        it.setAttribute(CuratorMenu.MarkTally.to(tally))
        it.textContent = tally.toMetricString()
    }

    // modify post lean
    val postLeanDelta = lean.value - document.querySelectorAll(CuratorMenu.UnmarkId.to(markId)).sumOf {
        if (it.getAttribute(AppAttribute.PostId) != postId || it.getAttribute(Attribute.IsOn) != true) 0
        else it.getAttribute(CuratorMenu.Lean)?.value ?: 0
    }
    document.querySelectorAll(CuratorMenu.PostLeanId.to(postId)).forEach {
        val postLean = it.requireAttribute(CuratorMenu.PostLean) + postLeanDelta
        it.setAttribute(CuratorMenu.PostLean.to(postLean))
        it.textContent = CuratorMenu.postLeanTextOf(postLean)
    }

    // unmark
    document.querySelectorAll(CuratorMenu.UnmarkId.to(markId)).forEach { unmarkElement ->
        if (unmarkElement.requireAttribute(AppAttribute.PostId) != postId) return@forEach
        unmarkElement.setAttribute(Attribute.IsOn.to(!isMarked))
        unmarkElement.getAttribute(CuratorMenu.MarkTally)?.let { tally ->
            val tallyNow = tally - 1
            unmarkElement.setAttribute(CuratorMenu.MarkTally.to(tallyNow))
        }
    }

    launchEffect {
        api.updateMark(MarkUpdate(markId, postId, isMarked)).toDataOrNull(toaster)
    }
}