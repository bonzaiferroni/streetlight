package streetlight.web.model

import streetlight.model.data.Layout
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import streetlight.model.data.TabsBlock
import kotlin.uuid.Uuid

class LayoutEditor(initialLayout: Layout) {
    private val blocks = mutableMapOf<Uuid, BlockEditor>()
    private val containers = mutableMapOf<Uuid, ContainerEditor>()

    val mainContainerId = buildContainer(initialLayout).id

    fun getBlock(blockId: Uuid) = blocks.getValue(blockId)
    fun getContainer(containerId: Uuid) = containers.getValue(containerId)

    fun addBlock(block: LayoutBlock, containerId: Uuid, index: Int) {
        val blockId = buildBlock(block)
        val container = getContainer(containerId)
        container.addBlock(blockId, index)
    }

    fun buildContainer(container: LayoutContainer): ContainerKey {
        val containerId = Uuid.random()
        val blockIds = container.blocks.map { block ->
            buildBlock(block)
        }.toSet()
        containers[containerId] = ContainerEditor(containerId, container.name, blockIds)
        return ContainerKey(container.name, containerId)
    }

    private fun buildBlock(block: LayoutBlock): Uuid {
        val blockId = Uuid.random()
        val containers = block.getContainers()
        val containerKeys = containers?.map { subContainer ->
            buildContainer(subContainer)
        }
        blocks[blockId] = BlockEditor(blockId, block, containerKeys, this)
        return blockId
    }
}

fun LayoutBlock.getContainers(): List<LayoutContainer>? = when (this) {
    is TabsBlock -> tabs
    else -> null
}

data class ContainerKey(
    val name: String,
    val id: Uuid,
)