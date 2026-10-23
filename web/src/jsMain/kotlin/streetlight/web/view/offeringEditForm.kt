package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.checkBox
import koala.dom.component.row
import koala.dom.component.textField
import koala.dom.flowValid
import koala.dom.markdown.markdownEditor
import koala.modifier.*
import streetlight.model.data.OfferingProperty
import streetlight.model.data.OfferingTag
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.model.OfferingEditor

/** The form of the offering [model] edits. */
context(model: OfferingEditor)
fun ViewScope.offeringEditForm() = formColumn {
    val heading = if (model.stateNow.edit.offeringId == null) "New Offering" else "Edit Offering"

    formRow {
        formSection(heading) {
            textField(model.titleState, "title", maxLength = 50)
                .flowValid(OfferingProperty.Title, model.validityState, contentScope)
            row(AlignItemsCenter) {
                textField(model.priceState, "price", Flex1, prefix = "$")
                    .flowValid(OfferingProperty.Price, model.validityState, contentScope)
                textField(model.platformCutState, "Streetlight cut", Flex1, suffix = "%")
                    .flowValid(OfferingProperty.PlatformCut, model.validityState, contentScope)
            }
            row(AlignItemsCenter) {
                OfferingTag.entries.forEach { tag ->
                    checkBox(model.tagState(tag), tag.label)
                }
            }
        }
        context(model.imageEditor) {
            imageFormSection("This image will appear with the offering.")
        }
    }
    formSection("Description") {
        markdownEditor(model.descriptionState, "description", MinHeight(32))
    }
}
