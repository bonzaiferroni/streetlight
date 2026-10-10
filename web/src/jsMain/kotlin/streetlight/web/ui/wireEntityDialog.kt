package streetlight.web.ui

import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.toDataOr
import koala.dom.ViewScope
import koala.dom.dialog
import koala.dom.flowBlock
import koala.dom.rawDialogContent
import koala.modifier.requireAttribute
import kotlinx.html.js.div
import streetlight.model.data.Entity
import streetlight.model.data.EntityRef
import streetlight.web.layouts.EntityDialog
import streetlight.web.layouts.EntityDialogStyle
import streetlight.web.layouts.entityDialog
import web.dom.Element
import web.events.Event
import web.html.HTMLElement

/** The record shown in the entity dialog; the dialog is open while it is set. */
private val entityRefState = storeOf<EntityRef?>(null)

/** The dialog showing the whole entity of a record, read when it opens, opened with [openEntityDialog]. */
fun ViewScope.wireEntityDialog() {
    val isOpen = entityRefState.mutableTapOf({ it != null }) { if (it) this else null }
    dialog(isOpen, EntityDialogStyle.Frame) {
        val ref = entityRefState.now ?: return@dialog
        val entityState = storeOf<Entity?>(null)
        launchEffect {
            val entity = api.entity.readEntity(ref).toDataOr(toaster) { return@launchEffect }
            entityState.set(entity)
        }
        rawDialogContent(null) {
            flowBlock(entityState) { entity ->
                entity?.let { div { entityDialog(it) } }
            }
        }
    }
}

/** Opens the entity dialog for the record named on [element], unless the click [event] landed on a link. */
fun openEntityDialog(element: HTMLElement, event: Event) {
    if ((event.target as? Element)?.closest("a") != null) return
    val type = element.requireAttribute(EntityDialog.Type)
    val slug = element.requireAttribute(EntityDialog.Slug)
    entityRefState.set(EntityRef(type, slug))
}
