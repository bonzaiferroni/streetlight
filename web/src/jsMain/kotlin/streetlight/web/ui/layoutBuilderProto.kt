package streetlight.web.ui

import koala.SvgFile
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.Height5
import koala.css.JustifyContentEnd
import koala.css.Zen
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.box
import koala.dom.button
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.popoverCard
import koala.dom.row
import koala.dom.tabs
import koala.dom.textBlock
import koala.dom.textField
import koala.html.Id
import koala.html.setPopoverTarget
import koala.model.MutableTap
import koala.model.tapOf
import koala.model.insertAt
import koala.model.keyedListField
import koala.model.keysField
import koala.model.mutableTapOf
import koala.model.storeOf
import koala.model.toggle
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LocationContent
import streetlight.model.data.TabContent
import streetlight.model.data.TabsBlock
import streetlight.model.data.TextBlock

fun ViewScope.containerBuilder(blocksField: MutableTap<List<LayoutBlock>>, content: LocationContent) {
    val keyedListField = blocksField.keyedListField()
    val keysField = keyedListField.keysField()
    flowBlock(keysField) { keys ->
        blockZone {
            blocksField.insertAt(0, it)
        }
        keys.forEachIndexed { index, key ->
            val blockField = keyedListField.mutableTapOf(key)
            blockBuilder(blockField, content)
            blockZone {
                blocksField.insertAt(index + 1, it)
            }
        }
    }
}

fun ViewScope.blockZone(onBlock: (LayoutBlock) -> Unit) {
    button(SvgFile.Plus, {
        onBlock(TextBlock("My Text"))
    })
}

fun ViewScope.blockBuilder(blockField: MutableTap<LayoutBlock>, content: LocationContent) {
    box {
//        when (val block = blockField.now) {
//            EventsBlock -> buildEvents(content)
//            HeaderBlock -> buildHeader(content)
//            is ImageBlock -> buildImage(block)
//            MapBlock -> buildMap(content.location.geoPoint)
//            is RichTextBlock -> buildRichText(block)
//             is TextBlock -> textBuilder(blockField.mutableFieldOf({ it as TextBlock}) { it })
//             is TabsBlock -> tabsBuilder(blockField.mutableFieldOf({ it as TabsBlock}) { it }, content)
//        }
    }
}

fun ViewScope.tabsBuilder(tabsField: MutableTap<TabsBlock>, content: LocationContent) {
    val tabNamesField = tabsField.tapOf { it.tabs.map {it.name} }
    val popoverId = Id("tabs-popover")
    popoverCard(popoverId) {
        flowBlock(tabNamesField) { tabNames ->
            column {
                tabNames.forEach { tabName ->
                    val isEditingField = storeOf(false)
                    flowBlock(isEditingField, modify(Height5)) { isEditing ->
                        if (isEditing) {
                            val tabNameField = storeOf(tabName)
                            textField(tabNameField, onEnter = {
                                tabsField.set { copy(tabs = tabs.map { tabContent ->
                                    if (tabContent.name == tabName) tabContent.copy(name = tabNameField.now) else tabContent
                                })}
                            })
                        } else {
                            row(modify(AlignItemsCenter, Height5)) {
                                textBlock(tabName, modify(Flex1))
                                button(SvgFile.Edit, { isEditingField.toggle() })
                                button(SvgFile.Minus, {
                                    tabsField.set { copy(tabs = tabs.filter { it.name != tabName }) }
                                })
                            }
                        }
                    }
                }
                button(SvgFile.Plus, {
                    val newTab = TabContent("new tab", emptyList())
                    tabsField.set { copy(tabs = tabs + newTab) }
                })
            }
        }
    }

    // val tabContentsField = tabsField.mutableFieldOf({ it.tabs } ) { copy(tabs = it)}
    // val keyedTabContentsField = tabContentsField.keyedListField()
    // val keysField = keyedTabContentsField.keysField()

    column {
        row(modify(JustifyContentEnd)) {
            button("tabs", mod = modify(Zen)) {
                setPopoverTarget(popoverId)
            }
        }
        flowBlock(tabsField) { tabsBlock ->
            tabs {
                tabsBlock.tabs.forEachIndexed { index, tabContent ->
                    tab(tabContent.name) {
                        val blocksField = tabsField.mutableTapOf({ it.tabs[index].blocks }) {
                            copy(tabs = tabs.mapIndexed { i, t -> if (i == index) t.copy(blocks = it) else t })
                        }
                        containerBuilder(blocksField, content)
                    }
                }
            }

            // println("tabNames: $tabNames")
            // println("tabFieldNames: ${tabsField.now.tabs.map { it.name }}")
            // tabs {
            //     keys.forEach { key ->
            //         val tabContent = keyedTabContentsField.now.first { it.key == key }.value
            //         val tabName = tabContent.name
            //         tab(tabName) {
            //             println("building: $tabName")
            //             val tabBlocksField = keyedTabContentsField.mutableFieldOf(key).mutableFieldOf({ it.blocks }) { copy(blocks = it)}
            //             containerBuilder(tabBlocksField, content)
            //         }
            //     }
            // }
        }
    }
}