package streetlight.web.model

import koala.dom.setStorageOf
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.LightEdit
import streetlight.model.data.LightRequest
import streetlight.model.data.MultiLightEdit
import kotlin.collections.minus
import kotlin.collections.plus

class LightCache<Id, Item>(
    private val cacheKey: String,
    val idToString: (Id) -> String,
    val stringToId: (String) -> Id,
    val itemToId: (Item) -> Id,
    private val lightEdit: suspend (LightRequest) -> Boolean?,
    private val readRemoteLights: suspend () -> Set<Id>?,
    private val readRemoteItems: suspend (List<Id>) -> List<Item>?,
    private val scope: CoroutineScope,
    private val gate: StarGate,
) {
    private val state = storeOf(LightCacheState<Id, Item>())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val lightsFlow = stateFlow.mapDistinct { it.lights }
    val itemsFlow = stateFlow.mapDistinct { it.items }

    private var cachedLights by setStorageOf(cacheKey, idToString, stringToId)

    init {
        scope.launch {
            launch {
                gate.signedInFlow.collect { isSignedIn ->
                    if (isSignedIn) {
                        if (cachedLights.isNotEmpty()) {
                            val request = MultiLightEdit(cachedLights.map { LightEdit(idToString(it), true) })
                            // send lights cached while signed out
                            lightEdit(request)
                            cachedLights = emptySet()
                        }
                        val lights = readRemoteLights() ?: return@collect
                        state.set { it.copy(lights = lights)}
                    }
                }
            }
            launch {
                lightsFlow.collect { lights ->
                    val newIds = lights.filter { lightId -> stateNow.items.none { itemToId(it) == lightId } }
                    val newItems = when (newIds.isEmpty()) {
                        true -> emptyList()
                        else -> readRemoteItems(newIds.toList()) ?: emptyList() // td: fail message
                    }
                    state.set { it.copy(items = it.items.filter { item -> lights.contains(itemToId(item)) } + newItems) }
                }
            }
        }
    }

    fun toggleLight(id: Id) {
        val isStar = stateNow.lights.contains(id)
        when (isStar) {
            true -> removeLight(id)
            else -> addLight(id)
        }
    }

    fun addLight(id: Id) = editLight(id, true)
    fun removeLight(id: Id) = editLight(id, false)

    private fun editLight(id: Id, isLit: Boolean) {
        when (gate.stateNow.isSignedIn) {
            true -> {
                scope.launch {
                    val edit = LightEdit(idToString(id), isLit)
                    val isSuccess = lightEdit(edit) ?: return@launch // td: ui message
                    if (isSuccess)
                        editState(id, isLit)
                }
            }
            else -> {
                when (isLit) {
                    true -> cachedLights += id
                    else -> cachedLights -= id
                }
                editState(id, isLit)
            }
        }
    }

    private fun editState(id: Id, isLit: Boolean) {
        when (isLit) {
            true -> state.set { it.copy(lights = it.lights + id) }
            else -> state.set { it.copy(lights = it.lights - id) }
        }
    }

    private suspend fun readLights() = when(gate.stateNow.isSignedIn) {
        true -> readRemoteLights() ?: emptySet()
        else -> cachedLights
    }
}

data class LightCacheState<Id, Item>(
    val lights: Set<Id> = emptySet(),
    val items: List<Item> = emptyList(),
)