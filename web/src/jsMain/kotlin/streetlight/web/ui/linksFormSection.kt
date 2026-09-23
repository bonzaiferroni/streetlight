package streetlight.web.ui

import kampfire.model.MutableTap
import kampfire.model.toValidAbsoluteUrlOrNull
import koala.SvgFile
import koala.modifier.*
import koala.dom.*
import koala.html.heading4
import koala.html.spacer
import koala.html.textBlock
import kampfire.model.append
import kampfire.model.removeAt
import kampfire.model.replaceAt
import kampfire.model.storeOf
import kampfire.model.toggle
import streetlight.model.data.ExtraLink

fun ViewScope.linksFormSection(linksState: MutableTap<List<ExtraLink>>) {
    formSection("additional links") {
        row(AlignItemsEnd) {
            column(Flex1) {
                heading4("Additional Links")
                textBlock("You may provide your original source, a youtube video, or any useful link.", InkDimFg)
            }
            button("➕ Add link", onClick = {
                linksState.append(ExtraLink.Empty)
            })
        }
        indexedItemsBlock(linksState, modify(Magic, Blur, SlideLeft)) { (index, link) ->
            val isEditingState = storeOf(link.label.isEmpty() || link.url.value.isEmpty())
            flowBlock(isEditingState) { isEditing ->
                row(AlignItemsCenter) {
                    if (isEditing) {
                        val labelState = storeOf(link.label)
                        val urlState = storeOf(link.url.value)
                        textField(labelState, "label")
                        textField(urlState, "url", Flex1)
                        button(SvgFile.Check, {
                            val label = labelState.now.takeIf { it.isNotEmpty() } ?: return@button
                            val url = urlState.now.takeIf { it.isNotBlank() }?.toValidAbsoluteUrlOrNull() ?: return@button
                            linksState.replaceAt(index, ExtraLink(label, url))
                        }, Height(5))
                    } else {
                        textBlock(link.label)
                        textBlock(link.url.value, InkDimFg)
                        spacer(Flex1)
                        button(SvgFile.Edit, isEditingState::toggle)
                        button(SvgFile.Trash, onClick = { linksState.removeAt(index) })
                    }
                }
            }
        }
    }
}
