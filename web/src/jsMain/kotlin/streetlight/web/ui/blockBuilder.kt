package streetlight.web.ui

import initElement
import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.Height5
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.row
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.textField
import koala.model.storeOf
import koala.model.toggle
import kotlinx.html.FlowContent
import org.w3c.dom.HTMLElement
import streetlight.model.data.*
import streetlight.web.layouts.buildEvents
import streetlight.web.layouts.buildFooter
import streetlight.web.layouts.buildHeader
import streetlight.web.layouts.buildImage
import streetlight.web.layouts.buildMap
import streetlight.web.layouts.buildRichText
import streetlight.web.model.BlockEditor
import streetlight.web.model.BlockId
import streetlight.web.model.LayoutEditor

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: BlockId, content: LocationContent) {
    val editor = model.getBlock(blockId)
    val element: HTMLElement? = when (val block = editor.blockField.now) {
        HeaderBlock -> blockBuilder("header", editor) {
            buildHeader(content)
        }
        EventsBlock -> blockBuilder("events", editor) {
            buildEvents(content)
        }
        is ImageBlock -> blockBuilder("image", editor) {
            buildImage(block)
        }
        MapBlock -> blockBuilder("map", editor) {
            buildMap(content.location.geoPoint)
        }
        is RichTextBlock -> blockBuilder("rich text", editor) {
            buildRichText(block)
        }
        is TextBlock -> textBuilder(editor)
        is TabsBlock -> tabsBuilder(editor, content)
    }

    element?.let {
        initElement(element)
    }
}

fun ViewScope.blockBuilder(
    name: String,
    editor: BlockEditor,
    onClick: (() -> Unit)?,
    content: ViewScope.() -> Unit
) {
    column {
        editorRow(name, editor, onClick)
        content()
    }
}

fun ViewScope.blockBuilder(
    name: String,
    editor: BlockEditor,
    content: FlowContent.() -> Unit
) = column {
    editorRow(name, editor, null)
    content()
}

fun ViewScope.textBuilder(editor: BlockEditor): HTMLElement? {
    val textField = editor.mutableFieldOf<TextBlock, String>({ it.text }) { copy(text = it) }
    val isEditingField = storeOf(textField.now.isEmpty())
    blockBuilder("text", editor, isEditingField::toggle) {
        flowBlock(isEditingField) { isEditing ->
            if (isEditing) {
                textField(textField)
            } else {
                textBlock(textField.now.takeIf { it.isNotEmpty() } ?: "[Text content]")
            }
        }
    }
    return null
}

fun ViewScope.tabsBuilder(editor: BlockEditor, content: LocationContent): HTMLElement? {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    column {
        menuEditRow("tabs", editor) {
            flowBlock(editor.refreshField) {
                column {
                    editor.childIds.forEach { containerId ->
                        val tabName = editor.model.getContainer(containerId).name
                        val isEditingField = storeOf(false)
                        flowBlock(isEditingField, modify(Height5)) { isEditing ->
                            if (isEditing) {
                                val tabNameField = storeOf(tabName)
                                textField(tabNameField, onEnter = {
                                    editor.renameContainer(containerId, tabNameField.now)
                                })
                            } else {
                                row(modify(AlignItemsCenter, Height5)) {
                                    textBlock(tabName, modify(Flex1))
                                    button(SvgFile.Edit, { isEditingField.toggle() })
                                    button(SvgFile.Minus, {
                                        editor.removeContainer(containerId)
                                    })
                                }
                            }
                        }
                    }
                    button(SvgFile.Plus, {
                        editor.addContainer(TabContent("new tab", emptyList()))
                    })
                }
            }
        }

        flowBlock(editor.refreshField) {
            tabs {
                editor.childIds.forEach { containerId ->
                    val container = editor.model.getContainer(containerId)
                    tab(container.name) {
                        containerBuilder(editor.model, containerId, content)
                    }
                }
            }
        }
    }
    return null
}