package streetlight.web.interop

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toDataOr
import koala.dom.ViewScope
import koala.dom.component.dialog
import koala.dom.layout.flowBlock
import koala.dom.component.rawDialogContent
import koala.modifier.requireAttribute
import kotlinx.html.js.div
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.web.layout.EntityDialog
import streetlight.web.layout.EntityDialogStyle
import streetlight.web.layout.entityDialog
import web.dom.Element
import web.events.Event
import web.html.HTMLElement
import streetlight.web.model.api
import streetlight.web.utility.element
import streetlight.web.model.toaster

/** The record shown in the entity dialog; the dialog is open while it is set. */
private val entityRefState = storeOf<EntityRef?>(null)

/** The dialog showing the whole entity of a record, read when it opens, opened with [readEntityRef]. */
fun ViewScope.wireEntityDialog() {
    val isOpen = entityRefState.mutableTapOf({ it != null }) { if (it) this else null }
    dialog(isOpen, EntityDialogStyle.Frame) {
        val ref = entityRefState.now ?: return@dialog
        val localEntityState = storeOf<Entity?>(null)
        launchEffect {
            val entity = api.entity.readEntity(ref).toDataOr(toaster) { return@launchEffect }
            localEntityState.set(entity)
        }
        rawDialogContent(null) {
            flowBlock(localEntityState) { entity ->
                entity?.let { div { entityDialog(it) } }
            }
        }
    }
}

/** Opens the entity dialog for the record named on [element], unless the click [event] landed on a link. */
fun readEntityRef(element: HTMLElement, event: Event) {
    if ((event.target as? Element)?.closest("a") != null) return
    val type = element.requireAttribute(EntityDialog.Type)
    val slug = element.requireAttribute(EntityDialog.Slug)
    entityRefState.set(EntityRef(type, slug))
}
