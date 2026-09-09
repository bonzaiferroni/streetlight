package streetlight.web.interop

import kabinet.utils.toMetricString
import kampfire.model.toDataOrNull
import koala.dom.ViewScope
import koala.dom.getAttribute
import koala.dom.querySelectorAll
import koala.dom.requireAttribute
import koala.dom.requireClosest
import koala.dom.setAttribute
import koala.html.Attribute
import koala.html.plus
import streetlight.model.data.CuratorStatus
import streetlight.model.data.MarkId
import streetlight.model.data.MarkUpdate
import streetlight.web.ui.AppAttribute
import streetlight.web.ui.CuratorMenu
import streetlight.web.ui.api
import streetlight.web.ui.toaster
import web.dom.Element
import web.dom.document
import web.html.HTMLElement

fun ViewScope.queryAndUpdateMark(element: HTMLElement): CuratorStatus {
    val markId = element.requireAttribute(CuratorMenu.MarkButtonId)
    val baseElement = element.requireClosest(CuratorMenu.CuratorJson)
    return updateMark(markId, baseElement)
}

fun ViewScope.updateMark(markId: MarkId, baseElement: Element): CuratorStatus {
    val curator = baseElement.requireAttribute(CuratorMenu.CuratorJson).toggleMark(markId)
    baseElement.setAttribute(CuratorMenu.CuratorJson.to(curator))
    baseElement.applyCurator(curator)

    val isMarked = curator.marks.first { it.markId == markId }.isMarked
    launchEffect {
        api.updateMark(MarkUpdate(markId, curator.postId, isMarked)).toDataOrNull(toaster)
    }
    return curator
}

fun CuratorStatus.toggleMark(markId: MarkId): CuratorStatus {
    val unmarkId = marks.find { it.markId == markId }?.unmarkId
    val isMarked = !marks.first { it.markId == markId}.isMarked

    return copy(
        marks = marks.map { mark ->
            when {
                mark.markId == markId -> mark.copy(
                    count = mark.count + if (isMarked) 1 else -1,
                    isMarked = isMarked
                )
                isMarked && mark.markId == unmarkId && mark.isMarked -> mark.copy(
                    count = mark.count - 1,
                    isMarked = false
                )
                else -> mark
            }
        }
    )
}

fun Element.applyCurator(curator: CuratorStatus) {
    curator.marks.forEach { mark ->
        // modify tally text
        querySelectorAll(CuratorMenu.MarkTallyId.to(mark.markId)).forEach {
            it.textContent = mark.count.toMetricString()
        }

        // modify mark buttons
        querySelectorAll(CuratorMenu.MarkButtonId.to(mark.markId)).forEach {
            it.setAttribute(Attribute.IsOn.to(mark.isMarked))
        }
    }

    querySelectorAll(CuratorMenu.PostLean).forEach {
        println(curator.postLean)
        it.textContent = CuratorMenu.postLeanTextOf(curator.postLean)
    }
}