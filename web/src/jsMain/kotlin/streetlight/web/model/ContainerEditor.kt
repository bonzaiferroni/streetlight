package streetlight.web.model

import koala.model.mutableFieldOf
import koala.model.storeOf
import streetlight.model.data.LayoutBlock
import kotlin.uuid.Uuid

class ContainerEditor(
    val containerId: Uuid,
    name: String,
    blockIds: Set<Uuid>,
    depth: Int,
    val model: LayoutEditor,
) {
    private val state = storeOf(ContainerState(name, blockIds, depth))
    val blockIdsField = state.mutableFieldOf({ it.blockIds }) { copy(blockIds = it) }
    val blockIds get() = state.now.blockIds
    val depth get() = state.now.depth

    fun addBlock(block: LayoutBlock) {
        model.addBlock(block, containerId, blockIds.size)
    }

    fun addBlock(blockId: Uuid, index: Int) {
        val newBlockIds = blockIdsField.now.toMutableList()
        newBlockIds.add(index, blockId)
        blockIdsField.set(newBlockIds.toSet())
    }
}

data class ContainerState(
    val name: String,
    val blockIds: Set<Uuid>,
    val depth: Int,
)