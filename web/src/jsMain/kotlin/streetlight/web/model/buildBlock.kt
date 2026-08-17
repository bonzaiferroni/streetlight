package streetlight.web.model

import streetlight.model.data.*

fun buildBlock(block: LayoutBlock, containers: List<ContainerDefinition>?): LayoutBlock? {
    return when (block) {
        is TabsBlock -> {
            val tabContents = containers?.takeIf { it.isNotEmpty() }?.map {
                TabContent(it.name, it.blocks)
            } ?: return null
            TabsBlock(tabContents)
        }
        is ColumnsBlock -> {
            block.copy(blocks = containers?.firstOrNull()?.blocks ?: return block)
        }
        else -> block
    }
}