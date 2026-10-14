package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.checkBox
import koala.dom.component.row
import koala.dom.component.textField
import koala.dom.flowValid
import koala.dom.launchEffect
import koala.dom.markdown.markdownEditor
import koala.model.MenuAction
import koala.model.MessageStore
import koala.modifier.*
import streetlight.model.data.OfferingProperty
import streetlight.model.data.OfferingTag
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.OfferingEditor
import streetlight.web.model.SellerConfig

/** The form of the offering [model] edits, which returns to [config]'s list once saved or abandoned. */
context(model: OfferingEditor, config: SellerConfig)
fun ViewScope.offeringEditForm() = formColumn {
    val messages = MessageStore()
    val heading = if (model.stateNow.edit.offeringId == null) "New Offering" else "Edit Offering"

    formRow {
        formSection(heading) {
            textField(model.titleField, "title", maxLength = 50)
                .flowValid(OfferingProperty.Title, model.validityField, contentScope)
            row(AlignItemsCenter) {
                textField(model.priceField, "price", Flex1)
                    .flowValid(OfferingProperty.Price, model.validityField, contentScope)
                textField(model.platformCutField, "Streetlight cut (%)", Flex1)
                    .flowValid(OfferingProperty.PlatformCut, model.validityField, contentScope)
            }
            row(AlignItemsCenter) {
                OfferingTag.entries.forEach { tag ->
                    checkBox(model.tagField(tag), tag.label)
                }
            }
        }
        context(model.imageEditor) {
            imageFormSection("This image will appear with the offering.")
        }
    }
    formSection("Description") {
        markdownEditor(model.descriptionField, "description", MinHeight(32))
    }
    formSubmit(
        label = "Save",
        onClick = {
            launchEffect {
                val offering = model.submitSuspend(messages) ?: return@launchEffect
                config.finishOfferingEdit(offering)
            }
        },
        messenger = messages,
        back = MenuAction("back", onClick = config::cancelOfferingEdit),
    )
}
