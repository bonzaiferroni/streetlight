package streetlight.web.ui

import kampfire.model.toValidAbsoluteUrlOrNull
import koala.SvgFile
import koala.css.*
import koala.dom.*
import koala.html.heading4
import koala.html.spacer
import koala.html.textBlock
import koala.model.append
import koala.model.removeAt
import koala.model.replaceAt
import koala.model.storeOf
import koala.model.toggle
import streetlight.model.data.ExtraLink
import streetlight.web.model.EventEditor

fun ViewScope.eventLinksForm(model: EventEditor) {
    formCard("Event Links") {
        formRow {
            formSection("Website") {
                textField(model.urlState, "Website", modify(Width100P))
                formText("Is there more information about this event on the web?")
            }
            formSection("Original Source") {
                textField(model.originalSourceLabelState, "Source name")
                formText("Want to give a shout out to the original place where you found the event?")
                textField(model.originalSourceUrlState, "Source url")
            }
        }

        formSection("additional links") {
            // td: modernize, it be broken
            row(modify(AlignItemsEnd)) {
                column(modify(Flex1)) {
                    heading4("Additional Links")
                    textBlock("You may provide your original source, a youtube video, or any useful link.", modify(Dim))
                }
                button("➕ Add link", onClick = {
                    model.linksState.append(ExtraLink.Empty)
                })
            }
            indexedItemsBlock(model.linksState, defaultMagic) { (index, link) ->
                val isEditingState = storeOf(link.label.isEmpty() || link.url.value.isEmpty())
                flowBlock(isEditingState) { isEditing ->
                    row(modify(AlignItemsCenter)) {
                        if (isEditing) {
                            val labelState = storeOf(link.label)
                            val urlState = storeOf(link.url.value)
                            textField(labelState, "label")
                            textField(urlState, "url", modify(Flex1))
                            button(SvgFile.Check, {
                                val label = labelState.now.takeIf { it.isNotEmpty() } ?: return@button
                                val url = urlState.now.takeIf { it.isNotBlank() }?.toValidAbsoluteUrlOrNull() ?: return@button
                                model.linksState.replaceAt(index, ExtraLink(label, url))
                            }, modify(Height5))
                        } else {
                            textBlock(link.label)
                            textBlock(link.url.value, modify(Dim))
                            spacer(modify(Flex1))
                            button(SvgFile.Edit, isEditingState::toggle)
                            button(SvgFile.Trash, onClick = { model.linksState.removeAt(index) })
                        }
                    }
                }
            }
        }
    }
}