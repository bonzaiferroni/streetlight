package streetlight.web.model

import koala.utils.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Layout
import streetlight.model.data.LayoutBlock
import streetlight.model.data.LayoutContainer
import streetlight.model.data.LocationId
import streetlight.model.data.TabsBlock
import streetlight.web.io.ApiClient
import kotlin.uuid.Uuid

class LayoutEditor(initialLayout: Layout) {
    private val blocks = mutableMapOf<Uuid, BlockEditor>()
    private val containers = mutableMapOf<Uuid, ContainerEditor>()

    val mainContainerId = addContainer(initialLayout).id

    fun getBlock(blockId: Uuid) = blocks.getValue(blockId)
    fun getContainer(containerId: Uuid) = containers.getValue(containerId)

    fun addBlock(block: LayoutBlock, containerId: Uuid, index: Int) {
        val blockId = addBlock(block)
        val container = getContainer(containerId)
        container.addBlock(blockId, index)
    }

    fun addBlockAbove(blockId: Uuid, block: LayoutBlock) {
        val container = getContainerWithBlock(blockId)
        val index = container.blockIds.indexOf(blockId)
        addBlock(block, container.containerId, index)
    }

    fun getContainerWithBlock(blockId: Uuid) = containers.firstNotNullOf {
        if (it.value.blockIds.contains(blockId)) it.value else null
    }

    fun addContainer(container: LayoutContainer): ContainerKey {
        val containerId = Uuid.random()
        val blockIds = container.blocks.map { block ->
            addBlock(block)
        }.toSet()
        containers[containerId] = ContainerEditor(containerId, container.name, blockIds)
        return ContainerKey(container.name, containerId)
    }

    private fun addBlock(block: LayoutBlock): Uuid {
        val blockId = Uuid.random()
        val containers = block.getContainers()
        val containerKeys = containers?.map { subContainer ->
            addContainer(subContainer)
        }
        blocks[blockId] = BlockEditor(blockId, block, containerKeys, this)
        return blockId
    }

    fun buildLayout(): Layout? {
        val blocks = buildContainer(mainContainerId).takeIf { it.isNotEmpty() } ?: return null
        return Layout(blocks)
    }

    private fun buildContainer(containerId: Uuid): List<LayoutBlock> {
        val container = getContainer(containerId)

        return container.blockIds.mapNotNull { blockId ->
            buildBlock(blockId)
        }
    }

    private fun buildBlock(blockId: Uuid): LayoutBlock? {
        val blockEditor = getBlock(blockId)
        val block = blockEditor.blockField.now
        val containers = blockEditor.containerKeys?.map { key ->
            ContainerDefinition(key.name, buildContainer(key.id))
        }
        return buildBlock(block, containers)
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

data class ContainerDefinition(
    val name: String,
    val blocks: List<LayoutBlock>
)