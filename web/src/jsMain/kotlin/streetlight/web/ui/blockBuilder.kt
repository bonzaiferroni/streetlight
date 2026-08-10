package streetlight.web.ui

import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.Height5
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.box
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.row
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.textField
import koala.model.storeOf
import koala.model.toggle
import streetlight.model.data.EventsBlock
import streetlight.model.data.HeaderBlock
import streetlight.model.data.ImageBlock
import streetlight.model.data.LocationContent
import streetlight.model.data.MapBlock
import streetlight.model.data.RichTextBlock
import streetlight.model.data.TabContent
import streetlight.model.data.TabsBlock
import streetlight.model.data.TextBlock
import streetlight.web.layouts.buildEvents
import streetlight.web.layouts.buildHeader
import streetlight.web.layouts.buildImage
import streetlight.web.layouts.buildMap
import streetlight.web.layouts.buildRichText
import streetlight.web.model.BlockEditor
import streetlight.web.model.LayoutEditor
import kotlin.uuid.Uuid

fun ViewScope.blockBuilder(model: LayoutEditor, blockId: Uuid, content: LocationContent) {
    val editor = model.getBlock(blockId)
    box {
        when (val block = editor.blockField.now) {
            EventsBlock -> buildEvents(content)
            HeaderBlock -> buildHeader(content)
            is ImageBlock -> buildImage(block)
            MapBlock -> buildMap(content.location.geoPoint)
            is RichTextBlock -> buildRichText(block)
            is TextBlock -> textBuilder(editor)
            is TabsBlock -> tabsBuilder(editor, content)
        }
    }
}

fun ViewScope.textBuilder(editor: BlockEditor) {
    val textField = editor.mutableFieldOf<TextBlock, String>({ it.text }) { copy(text = it) }
    val isEditingField = storeOf(false)
    column {
        editorRow("text", editor, isEditingField::toggle)
        flowBlock(isEditingField) { isEditing ->
            if (isEditing) {
                textField(textField)
            } else {
                textBlock(textField.now)
            }
        }
    }
}

fun ViewScope.tabsBuilder(editor: BlockEditor, content: LocationContent) {
    // val tabsField = editor.blockField.narrow<LayoutBlock, TabsBlock>()
    column {
        menuEditRow("tabs", editor) {
            flowBlock(editor.refreshField) {
                column {
                    editor.containerKeys?.forEach { (tabName, containerId) ->
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
                editor.containerKeys?.forEach { (name, containerId) ->
                    tab(name) {
                        containerBuilder(editor.model, containerId, content)
                    }
                }
            }
        }
    }
}