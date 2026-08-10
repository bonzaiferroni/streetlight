package streetlight.web.model

import koala.model.mutableFieldOf
import koala.model.storeOf
import kotlin.uuid.Uuid

class ContainerEditor(
    val containerId: Uuid,
    name: String,
    blockIds: Set<Uuid>,
) {
    private val state = storeOf(ContainerState(name, blockIds))
    val blockIdsField = state.mutableFieldOf({ it.blockIds }) { copy(blockIds = it) }
    val blockIds get() = state.now.blockIds

    fun addBlock(blockId: Uuid, index: Int) {
        val newBlockIds = blockIdsField.now.toMutableList()
        newBlockIds.add(index, blockId)
        blockIdsField.set(newBlockIds.toSet())
    }
}

data class ContainerState(
    val name: String,
    val blockIds: Set<Uuid>,
)