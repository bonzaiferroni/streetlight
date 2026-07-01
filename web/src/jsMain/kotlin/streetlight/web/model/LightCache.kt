package streetlight.web.model

import kampfire.model.Outcome
import kampfire.model.handleOutcome
import koala.dom.setStorageOf
import koala.model.mapDistinct
import koala.model.storeOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import streetlight.model.data.LightEdit
import streetlight.model.data.EditLightRequest
import streetlight.model.data.LightType
import streetlight.model.data.MultiLightEdit
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.uuid.Uuid

class LightCache<Id, Item>(
    val lightType: LightType,
    private val cacheKey: String,
    val idToUuid: (Id) -> Uuid,
    val uuidToId: (Uuid) -> Id,
    val itemToId: (Item) -> Id,
    private val lightEdit: suspend (EditLightRequest) -> Outcome<Boolean>?,
    private val readRemoteLights: suspend () -> Outcome<List<Id>>?,
    private val readRemoteItems: suspend (List<Id>) -> Outcome<List<Item>>?,
    private val onError: (String) -> Unit,
    private val scope: CoroutineScope,
    private val gate: StarSession,
) {
    private val state = storeOf(LightCacheState<Id, Item>())
    val stateNow get() = state.now
    val stateFlow = state.flow
    val lightsFlow = stateFlow.mapDistinct { it.lights }
    val itemsFlow = stateFlow.mapDistinct { it.items }

    private val idToString: (Id) -> String = { idToUuid(it).toString() }
    private val stringToId: (String) -> Id = { uuidToId(Uuid.parse(it)) }

    private var cachedLights by setStorageOf(cacheKey, idToString, stringToId)

    init {
        scope.launch {
            launch {
                gate.signedInFlow.collect { isSignedIn ->
                    if (isSignedIn) {
                        if (cachedLights.isNotEmpty()) {
                            val request = MultiLightEdit(cachedLights.map { LightEdit(idToUuid(it), true, lightType) })
                            // send lights cached while signed out
                            lightEdit(request)
                            cachedLights = emptySet()
                        }
                        val lights = readRemoteLights().handleOutcome(onError) ?: return@collect
                        state.set { it.copy(lights = lights.toSet())}
                    }
                }
            }
            launch {
                lightsFlow.collect { lights ->
                    val newIds = lights.filter { lightId -> stateNow.items.none { itemToId(it) == lightId } }
                    val newItems = when (newIds.isEmpty()) {
                        true -> emptyList()
                        else -> readRemoteItems(newIds.toList()).handleOutcome(onError) ?: emptyList() // td: fail message
                    }
                    state.set { it.copy(items = it.items.filter { item -> lights.contains(itemToId(item)) } + newItems) }
                }
            }
        }
    }

    fun toggleLight(id: Id): Boolean {
        val isLitNow = stateNow.lights.contains(id)
        when (isLitNow) {
            true -> removeLight(id)
            else -> addLight(id)
        }
        return !isLitNow
    }

    fun addLight(id: Id) = editLight(id, true)
    fun removeLight(id: Id) = editLight(id, false)

    private fun editLight(id: Id, isLit: Boolean) {
        when (gate.stateNow.isSignedIn) {
            true -> {
                scope.launch {
                    val edit = LightEdit(idToUuid(id), isLit, lightType)
                    val isSuccess = lightEdit(edit).handleOutcome(onError) ?: return@launch
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
        true -> readRemoteLights().handleOutcome(onError) ?: emptySet()
        else -> cachedLights
    }
}

data class LightCacheState<Id, Item>(
    val lights: Set<Id> = emptySet(),
    val items: List<Item> = emptyList(),
)